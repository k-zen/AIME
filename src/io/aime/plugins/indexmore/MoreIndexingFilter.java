package io.aime.plugins.indexmore;

import io.aime.crawl.CrawlDatum;
import io.aime.crawl.Inlinks;
import io.aime.indexer.AIMEDocument;
import io.aime.indexer.IndexingException;
import io.aime.indexer.IndexingFilter;
import io.aime.metadata.DocMetadata;
import io.aime.net.protocols.HTTPDateFormat;
import io.aime.net.protocols.Response;
import io.aime.parse.Parse;
import io.aime.parse.ParseData;
import io.aime.util.MimeUtil;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.lang.time.DateUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.log4j.Logger;

public class MoreIndexingFilter implements IndexingFilter
{

    private static final Logger LOG = Logger.getLogger(MoreIndexingFilter.class.getName());
    private static Pattern patterns[] =
    {
        null, null
    };
    private MimeUtil MIME;
    private Configuration conf;

    static
    {
        try
        {
            // Order here is important.
            MoreIndexingFilter.patterns[0] = Pattern.compile("\\bfilename=['\"](.+)['\"]");
            MoreIndexingFilter.patterns[1] = Pattern.compile("\\bfilename=(\\S+)\\b");
        }
        catch (PatternSyntaxException e)
        {
            // Ignore.
        }
    }

    @Override
    public AIMEDocument filter(AIMEDocument doc, Parse parse, Text url, CrawlDatum datum, Inlinks inlinks) throws IndexingException
    {
        String urlS = url.toString();

        this.addTime(doc, parse.getData(), urlS, datum);
        this.addLength(doc, parse.getData(), urlS);
        this.addType(doc, parse.getData(), urlS, datum);
        this.resetTitle(doc, parse.getData(), urlS);

        return doc;
    }

    private AIMEDocument addTime(AIMEDocument doc, ParseData data, String url, CrawlDatum datum)
    {
        long time = -1;
        String lastModified = data.getMeta(DocMetadata.LAST_MODIFIED);

        // Get the timestamp of the last modification date.
        if (lastModified != null)
        {
            time = this.getTime(lastModified, url);
            doc.add("lastmodified", String.valueOf(time));
        }

        // If the file doesn't have a last modification date, then use
        // the fetch time.
        if (time == -1)
        {
            time = datum.getFetchTime();
        }

        return doc;
    }

    /**
     * This method parses the Last-Modified string value, and returns a
     * timestamp.
     *
     * @param date The Last-Modified date.
     * @param url  The URL of the document.
     *
     * @return The timestamp that corresponds to the Last-Modified date.
     */
    private long getTime(String date, String url)
    {
        long time = -1;

        try
        {
            time = HTTPDateFormat.toLong(date);
        }
        catch (ParseException e)
        {
            try
            {
                Date parsedDate = DateUtils.parseDate(date,
                                                      new String[]
                {
                    "EEE MMM dd HH:mm:ss yyyy",
                    "EEE MMM dd HH:mm:ss yyyy zzz",
                    "EEE MMM dd HH:mm:ss zzz yyyy",
                    "EEE, MMM dd HH:mm:ss yyyy zzz",
                    "EEE, dd MMM yyyy HH:mm:ss zzz",
                    "EEE,dd MMM yyyy HH:mm:ss zzz",
                    "EEE, dd MMM yyyy HH:mm:sszzz",
                    "EEE, dd MMM yyyy HH:mm:ss",
                    "EEE, dd-MMM-yy HH:mm:ss zzz",
                    "yyyy/MM/dd HH:mm:ss.SSS zzz",
                    "yyyy/MM/dd HH:mm:ss.SSS",
                    "yyyy/MM/dd HH:mm:ss zzz",
                    "yyyy/MM/dd",
                    "yyyy.MM.dd HH:mm:ss",
                    "yyyy-MM-dd HH:mm",
                    "MMM dd yyyy HH:mm:ss. zzz",
                    "MMM dd yyyy HH:mm:ss zzz",
                    "dd.MM.yyyy HH:mm:ss zzz",
                    "dd MM yyyy HH:mm:ss zzz",
                    "dd.MM.yyyy; HH:mm:ss",
                    "dd.MM.yyyy HH:mm:ss",
                    "dd.MM.yyyy zzz",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:sszzz"
                                                      });
                time = parsedDate.getTime();
            }
            catch (ParseException ex)
            {
                LOG.warn("Impossible to parse erroneous date: " + date + " for URL: " + url);
            }
        }

        return time;
    }

    /**
     * This method adds the content length of the document to the index.
     *
     * @param doc  The AIME document.
     * @param data The ParseData object.
     * @param url  The URL of the document.
     *
     * @return The altered AIME document.
     */
    private AIMEDocument addLength(AIMEDocument doc, ParseData data, String url)
    {
        String contentLength = data.getMeta(Response.CONTENT_LENGTH);

        if (contentLength != null)
        {
            doc.add("contentlength", contentLength.trim());
        }

        return doc;
    }

    /**
     * Add Content-Type and its primaryType and subType add contentType,
     * primaryType and subType to field "type" as un-stored, indexed and
     * un-tokenized, so that search results can be confined by contentType or
     * its primaryType or its subType.
     *
     * <p>
     * For example, if contentType is application/vnd.ms-powerpoint, search
     * can be done with one of the following qualifiers
     * type:application/vnd.ms-powerpoint type:application
     * type:vnd.ms-powerpoint all case insensitive. The query filter is
     * implemented in {@link TypeQueryFilter}.</p>
     *
     * @param doc  The AIME document.
     * @param data The ParseData object.
     * @param url  The URL of the document.
     *
     * @return The altered AIME document.
     */
    private AIMEDocument addType(AIMEDocument doc, ParseData data, String url, CrawlDatum datum)
    {
        String mimeType;
        String contentType;

        Writable tcontentType = datum.getMetadata().get(new Text(Response.CONTENT_TYPE));
        if (tcontentType != null)
        {
            contentType = tcontentType.toString();
        }
        else
        {
            contentType = data.getMeta(Response.CONTENT_TYPE);
        }
        if (contentType == null)
        {
            mimeType = MIME.getMimeType(url);
        }
        else
        {
            mimeType = MIME.forName(MimeUtil.cleanMimeType(contentType));
        }

        // Checks if we solved the content-type.
        if (mimeType == null)
        {
            return doc;
        }

        contentType = mimeType;
        doc.add("filetype", contentType);

        return doc;
    }

    private AIMEDocument resetTitle(AIMEDocument doc, ParseData data, String url)
    {
        String contentDisposition = data.getMeta(DocMetadata.CONTENT_DISPOSITION);
        if (contentDisposition == null)
        {
            return doc;
        }
        for (Pattern pattern : MoreIndexingFilter.patterns)
        {
            Matcher matcher = pattern.matcher(contentDisposition);
            if (matcher.find())
            {
                doc.add("title", matcher.group(1));

                break;
            }
        }

        return doc;
    }

    @Override
    public void setConf(Configuration conf)
    {
        this.conf = conf;
        MIME = new MimeUtil(conf);
    }

    @Override
    public Configuration getConf()
    {
        return this.conf;
    }

    @Override
    public void close()
    {
        // Ignore.
    }

    @Override
    public void addIndexBackendOptions(Configuration conf)
    {
        // InternalTools.addFieldOptions("lastmodified", IndexServerImplementation.STORE.YES, IndexServerImplementation.INDEX.UNTOKENIZED, conf);
        // InternalTools.addFieldOptions("contentlength", IndexServerImplementation.STORE.YES, IndexServerImplementation.INDEX.NO, conf);
        // InternalTools.addFieldOptions("filetype", IndexServerImplementation.STORE.YES, IndexServerImplementation.INDEX.UNTOKENIZED, conf);
    }
}
