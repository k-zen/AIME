package io.aime.parse;

import io.aime.brain.Brain;
import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.MetadataGeneral;
import io.aime.brain.xml.Handler;
import io.aime.crawl.CrawlDatum;
import io.aime.fetcher.Fetcher;
import io.aime.net.URLFilter;
import io.aime.net.URLFilterException;
import io.aime.net.URLFilters;
import io.aime.net.URLNormalizers;
import io.aime.util.AIMEConstants;
import io.aime.util.URLUtil;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.InvalidJobConfException;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.util.Progressable;
import org.apache.log4j.Logger;

/**
 * Parse content in a segment.
 */
public class ParseOutputFormat implements OutputFormat<Text, Parse>
{

    private static final Logger LOG = Logger.getLogger(ParseOutputFormat.class.getName());
    private URLFilter[] filters;
    private URLNormalizers normalizers;

    private static class SimpleEntry implements Entry<Text, CrawlDatum>
    {

        private Text key;
        private CrawlDatum value;

        public SimpleEntry(Text key, CrawlDatum value)
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public Text getKey()
        {
            return key;
        }

        @Override
        public CrawlDatum getValue()
        {
            return value;
        }

        @Override
        public CrawlDatum setValue(CrawlDatum value)
        {
            this.value = value;
            return this.value;
        }
    }

    @Override
    public void checkOutputSpecs(FileSystem fs, JobConf job) throws IOException
    {
        Path out = FileOutputFormat.getOutputPath(job);

        // Check if the output dir is set.
        if ((out == null) && (job.getNumReduceTasks() != 0)) {
            throw new InvalidJobConfException("Output directory not set in JobConf!");
        }

        // Check if the filesystem object is not null.
        if (fs == null && out != null) {
            fs = out.getFileSystem(job);
        }

        // If we enter here, that means this segment was fetched previously.
        if (fs != null) {
            if (fs.exists(new Path(out, CrawlDatum.PARSE_DIR_NAME))) {
                throw new IOException("Segment already parsed!");
            }
        }
    }

    @Override
    public RecordWriter<Text, Parse> getRecordWriter(FileSystem fs, JobConf job, String name, Progressable progress) throws IOException
    {
        filters = (URLFilter[]) Brain
                .getClient(job)
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataGeneral.Data.class)
                                .setFunction(MetadataGeneral.Data.FILTERS.getMethodName()))).get();
        normalizers = new URLNormalizers(job, URLNormalizers.SCOPE_OUTLINK);
        final int interval = job.getInt("db.fetch.interval.default", 2592000);
        final boolean ignoreExternalLinks = job.getBoolean("db.ignore.external.links", false);
        int maxOutlinksPerPage = job.getInt("db.max.outlinks.per.page", 100);
        final int maxOutlinks = (maxOutlinksPerPage < 0) ? Integer.MAX_VALUE : maxOutlinksPerPage;
        final CompressionType compType = SequenceFileOutputFormat.getOutputCompressionType(job);
        Path out = FileOutputFormat.getOutputPath(job);
        Path text = new Path(new Path(out, ParseText.DIR_NAME), name);
        Path data = new Path(new Path(out, ParseData.DIR_NAME), name);
        Path crawl = new Path(new Path(out, CrawlDatum.PARSE_DIR_NAME), name);
        final String[] parseMDtoCrawlDB = job.get("db.parsemeta.to.crawldb", "").split(" *, *");
        final MapFile.Writer textOut = new MapFile.Writer(job, fs, text.toString(), Text.class, ParseText.class, CompressionType.RECORD, progress);
        final MapFile.Writer dataOut = new MapFile.Writer(job, fs, data.toString(), Text.class, ParseData.class, compType, progress);
        final SequenceFile.Writer crawlOut = SequenceFile.createWriter(fs, job, crawl, Text.class, CrawlDatum.class, compType, progress);

        return new RecordWriter<Text, Parse>()
        {
            @Override
            public void write(Text key, Parse parse) throws IOException
            {
                String fromUrl = key.toString();
                String fromHost;
                String toHost;
                textOut.append(key, new ParseText(parse.getText()));
                ParseData parseData = parse.getData();
                // recover the signature prepared by Fetcher or ParseSegment
                String sig = parseData.getContentMeta().get(AIMEConstants.SIGNATURE_KEY.getStringConstant());

                if (sig != null) {
                    long signature = Long.parseLong(sig);
                    // append a CrawlDatum with a signature
                    CrawlDatum d = new CrawlDatum(CrawlDatum.STATUS_SIGNATURE, 0);
                    d.setSignature(signature);
                    crawlOut.append(key, d);
                }

                // see if the parse metadata contain things that we'd like
                // to pass to the metadata of the crawlDB entry
                CrawlDatum parseMDCrawlDatum = null;
                for (String mdname : parseMDtoCrawlDB) {
                    String mdvalue = parse.getData().getParseMeta().get(mdname);

                    if (mdvalue != null) {
                        if (parseMDCrawlDatum == null) {
                            parseMDCrawlDatum = new CrawlDatum(CrawlDatum.STATUS_PARSE_META, 0);
                        }

                        parseMDCrawlDatum.getMetadata().put(new Text(mdname), new Text(mdvalue));
                    }
                }

                if (parseMDCrawlDatum != null) {
                    crawlOut.append(key, parseMDCrawlDatum);
                }

                try {
                    ParseStatus pstatus = parseData.getStatus();

                    if (pstatus != null && pstatus.isSuccess() && pstatus.getMinorCode() == ParseStatus.SUCCESS_REDIRECT) {
                        String newUrl = pstatus.getMessage();
                        int refreshTime = Integer.valueOf(pstatus.getArgs()[1]);

                        try {
                            newUrl = normalizers.normalize(newUrl, URLNormalizers.SCOPE_FETCHER);
                        }
                        catch (MalformedURLException mfue) {
                            newUrl = null;
                        }

                        if (newUrl != null) {
                            newUrl = URLFilters.filter(filters, newUrl);
                        }

                        String url = key.toString();

                        if (newUrl != null && !newUrl.equals(url)) {
                            String reprUrl = URLUtil.chooseRepr(url, newUrl, refreshTime < Fetcher.PERM_REFRESH_TIME);
                            CrawlDatum newDatum = new CrawlDatum();
                            newDatum.setStatus(CrawlDatum.STATUS_LINKED);

                            if (reprUrl != null && !reprUrl.equals(newUrl)) {
                                newDatum.getMetadata().put(AIMEConstants.WRITABLE_REPR_URL_KEY.getTextConstant(), new Text(reprUrl));
                            }

                            crawlOut.append(new Text(newUrl), newDatum);
                        }
                    }
                }
                catch (URLFilterException e) {
                }

                // collect outlinks for subsequent db update
                Outlink[] links = parseData.getOutlinks();
                int outlinksToStore = Math.min(maxOutlinks, links.length);

                if (ignoreExternalLinks) {
                    try {
                        fromHost = new URL(fromUrl).getHost().toLowerCase();
                    }
                    catch (MalformedURLException e) {
                        fromHost = null;
                    }
                }
                else {
                    fromHost = null;
                }

                int validCount = 0;
                CrawlDatum adjust = null;
                List<Entry<Text, CrawlDatum>> targets = new ArrayList<>(outlinksToStore);
                List<Outlink> outlinkList = new ArrayList<>(outlinksToStore);

                for (int i = 0; i < links.length && validCount < outlinksToStore; i++) {
                    String toUrl = links[i].getToUrl();

                    // ignore links to self (or anchors within the page)
                    if (fromUrl.equals(toUrl)) {
                        continue;
                    }

                    if (ignoreExternalLinks) {
                        try {
                            toHost = new URL(toUrl).getHost().toLowerCase();
                        }
                        catch (MalformedURLException e) {
                            toHost = null;
                        }

                        if (toHost == null || !toHost.equals(fromHost)) { // external links
                            continue; // skip it
                        }
                    }

                    try {
                        toUrl = normalizers.normalize(toUrl, URLNormalizers.SCOPE_OUTLINK); // normalize the url
                        toUrl = URLFilters.filter(filters, toUrl);   // filter the url

                        if (toUrl == null) {
                            continue;
                        }
                    }
                    catch (MalformedURLException | URLFilterException e) {
                        continue;
                    }

                    CrawlDatum target = new CrawlDatum(CrawlDatum.STATUS_LINKED, interval);
                    Text targetUrl = new Text(toUrl);

                    target.setScore(0.0f);

                    targets.add(new SimpleEntry(targetUrl, target));
                    outlinkList.add(links[i]);
                    validCount++;
                }

                for (Entry<Text, CrawlDatum> target : targets) {
                    crawlOut.append(target.getKey(), target.getValue());
                }

                if (adjust != null) {
                    crawlOut.append(key, adjust);
                }

                Outlink[] filteredLinks = outlinkList.toArray(new Outlink[outlinkList.size()]);
                parseData = new ParseData(
                        parseData.getStatus(),
                        parseData.getTitle(),
                        filteredLinks,
                        parseData.getContentMeta(),
                        parseData.getParseMeta(),
                        parseData.getContentBlocks());
                dataOut.append(key, parseData);

                if (!parse.isCanonical()) {
                    CrawlDatum datum = new CrawlDatum();
                    datum.setStatus(CrawlDatum.STATUS_FETCH_SUCCESS);
                    String timeString = parse.getData().getContentMeta().get(AIMEConstants.FETCH_TIME_KEY.getStringConstant());

                    try {
                        datum.setFetchTime(Long.parseLong(timeString));
                    }
                    catch (NumberFormatException e) {
                        LOG.warn("Impossible to read fetch time for: " + key);
                        datum.setFetchTime(System.currentTimeMillis());
                    }

                    crawlOut.append(key, datum);
                }
            }

            @Override
            public void close(Reporter reporter) throws IOException
            {
                textOut.close();
                dataOut.close();
                crawlOut.close();
            }
        };
    }
}
