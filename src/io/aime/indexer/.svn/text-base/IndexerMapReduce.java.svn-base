package io.aime.indexer;

// AIME
import io.aime.crawl.CrawlDatum;
import io.aime.crawl.CrawlDB;
import io.aime.crawl.Inlinks;
import io.aime.crawl.AIMEWritable;
import io.aime.metadata.DocMetadata;
import io.aime.parse.Parse;
import io.aime.parse.ParseData;
import io.aime.parse.ParseImplementation;
import io.aime.parse.ParseText;
import io.aime.util.AIMEConstants;
import io.aime.util.GeneralUtilities;

// Apache Hadoop
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;

// I/O
import java.io.IOException;

// Log4J
import org.apache.log4j.Logger;

// Util
import java.util.Iterator;

public class IndexerMapReduce extends Configured {

    private static final Logger LOG = Logger.getLogger(IndexerMapReduce.class.getName());

    public static class IndexerMapReduceMapper implements Mapper<Text, Writable, Text, AIMEWritable> {

        @Override
        public void configure(JobConf job) {
        }

        @Override
        public void map(Text key, Writable value, OutputCollector<Text, AIMEWritable> output, Reporter reporter) throws IOException {
            output.collect(key, new AIMEWritable(value));
        }

        @Override
        public void close() throws IOException {
        }
    }

    public static class IndexerMapReduceReducer implements Reducer<Text, AIMEWritable, Text, AIMEDocument> {

        private IndexingFilters filters;

        @Override
        public void configure(JobConf job) {
            this.filters = new IndexingFilters(job);
        }

        @Override
        public void reduce(Text key, Iterator<AIMEWritable> values, OutputCollector<Text, AIMEDocument> output, Reporter reporter) throws IOException {
            Inlinks inlinks = null;
            CrawlDatum dbDatum = null;
            CrawlDatum fetchDatum = null;
            ParseData parseData = null;
            ParseText parseText = null;

            while (values.hasNext()) {
                final Writable value = values.next().get(); // Unwrap

                if (value instanceof Inlinks) {
                    inlinks = (Inlinks) value;
                }
                else if (value instanceof CrawlDatum) {
                    final CrawlDatum datum = (CrawlDatum) value;

                    if (CrawlDatum.hasDBStatus(datum)) {
                        dbDatum = datum;
                    }
                    else if (CrawlDatum.hasFetchStatus(datum)) {
                        // Don't index unmodified (empty) pages.
                        if (datum.getStatus() != CrawlDatum.STATUS_FETCH_NOTMODIFIED) {
                            fetchDatum = datum;
                        }
                    }
                    else if (CrawlDatum.STATUS_LINKED == datum.getStatus() || CrawlDatum.STATUS_SIGNATURE == datum.getStatus() || CrawlDatum.STATUS_PARSE_META == datum.getStatus()) {
                        continue;
                    }
                    else {
                        throw new RuntimeException("Unexpected state: " + datum.getStatus());
                    }
                }
                else if (value instanceof ParseData) {
                    parseData = (ParseData) value;
                }
                else if (value instanceof ParseText) {
                    parseText = (ParseText) value;
                }
                else {
                    LOG.warn("Unrecognized type: " + value.getClass());
                }
            }

            if (fetchDatum == null || dbDatum == null || parseText == null || parseData == null) {
                return; // only have inlinks
            }

            if (!parseData.getStatus().isSuccess() || fetchDatum.getStatus() != CrawlDatum.STATUS_FETCH_SUCCESS) {
                return;
            }

            AIMEDocument doc = new AIMEDocument();
            final DocMetadata metadata = parseData.getContentMeta();

            doc.add("segment", metadata.get(AIMEConstants.SEGMENT_NAME_KEY.getStringConstant()));
            doc.add("digest", metadata.get(AIMEConstants.SIGNATURE_KEY.getStringConstant()));
            doc.add("boostwithgravity", "");
            doc.add("gravity", "");
            doc.add("itsecond", "");
            doc.add("itminute", "");
            doc.add("ithour", "");
            doc.add("itday", "");
            doc.add("itmonth", "");
            doc.add("ityear", "");
            doc.add("uniqueid", MD5Hash.digest(key.toString()).toString());

            final Parse parse = new ParseImplementation(parseText, parseData);
            try {
                // Extract information from dbDatum and pass it to
                // fetchDatum so that indexing filters can use it.
                final Text url = (Text) dbDatum.getMetadata().get(AIMEConstants.WRITABLE_REPR_URL_KEY.getTextConstant());

                if (url != null) {
                    fetchDatum.getMetadata().put(AIMEConstants.WRITABLE_REPR_URL_KEY.getTextConstant(), url);
                }

                // Run indexing filters
                doc = this.filters.filter(doc, parse, key, fetchDatum, inlinks);
            }
            catch (final IndexingException e) {
                LOG.warn("Error indexing: " + key + ". Error: " + e.toString(), e);

                return;
            }

            // Skip documents discarded by indexing filters
            if (doc == null) {
                return;
            }

            float boost = (dbDatum.getScore() > 0.0f) ? dbDatum.getScore() : 1.0f; // Guardar el puntaje.

            // Apply boost to all indexed fields.
            doc.setWeight(boost);

            // Store boost for use by explain and dedup
            doc.add("boost", Float.toString(boost));

            // Recolect
            output.collect(key, doc);
        }

        @Override
        public void close() throws IOException {
            this.filters.close();
            this.filters = null;
        }
    }

    /**
     * Configures an indexing job.
     *
     * @param job     Job's configuration object.
     * @param exeType The type of execution.
     */
    public static void configureIndexer(JobConf job, int exeType) {
        Path mainDBase = new Path(AIMEConstants.MAINDBASE_PATH.getStringConstant());

        // Grab only!!! the last segment.
        String choice = AIMEConstants.SEGMENTDBASE_PATH.getStringConstant() + "/*"; // Fallback all segments.
        if (exeType == AIMEConstants.LOCAL_EXECUTION_TYPE.getIntegerConstant()) {
            choice = GeneralUtilities.lastFileModified(AIMEConstants.SEGMENTDBASE_PATH.getStringConstant()).getAbsolutePath();
        }
        else {
            try {
                FileSystem fs = FileSystem.get(job);
                FileStatus[] fst = fs.listStatus(new Path(AIMEConstants.SEGMENTDBASE_PATH.getStringConstant()));
                long lastMod = Long.MIN_VALUE;

                for (FileStatus file : fst) {
                    if (file.getModificationTime() > lastMod) {
                        choice = file.getPath().toString();
                        lastMod = file.getModificationTime();
                    }
                }
            }
            catch (IOException e) {
                LOG.fatal("Error trying to find the last segment in DFS. Error: " + e.toString(), e);
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Using last segment fetched for indexing. Segment: " + choice);
        }
        Path segmDBase = new Path(choice);

        // Add input paths.
        FileInputFormat.addInputPath(job, new Path(segmDBase, CrawlDatum.FETCH_DIR_NAME));
        FileInputFormat.addInputPath(job, new Path(segmDBase, CrawlDatum.PARSE_DIR_NAME));
        FileInputFormat.addInputPath(job, new Path(segmDBase, ParseData.DIR_NAME));
        FileInputFormat.addInputPath(job, new Path(segmDBase, ParseText.DIR_NAME));
        FileInputFormat.addInputPath(job, new Path(mainDBase, CrawlDB.CURRENT_NAME));

        // Configure Job
        job.setInputFormat(SequenceFileInputFormat.class);
        job.setMapperClass(IndexerMapReduce.IndexerMapReduceMapper.class);
        job.setReducerClass(IndexerMapReduce.IndexerMapReduceReducer.class);
        job.setOutputFormat(IndexerOutputFormat.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(AIMEWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(AIMEDocument.class);
    }
}
