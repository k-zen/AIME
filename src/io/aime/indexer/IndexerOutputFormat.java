package io.aime.indexer;

// AIME
import io.aime.util.Timer;

// Apache Hadoop
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.Progressable;

// IO
import java.io.IOException;

// Log4j
import org.apache.log4j.Logger;

public class IndexerOutputFormat implements OutputFormat<Text, AIMEDocument> {

    private static final Logger LOG = Logger.getLogger(IndexerOutputFormat.class.getName());
    private static Timer timer = new Timer();

    @Override
    public RecordWriter<Text, AIMEDocument> getRecordWriter(FileSystem ignored, final JobConf job, String name, Progressable progress) throws IOException {
        return new RecordWriter<Text, AIMEDocument>() {
            @Override
            public void close(Reporter reporter) throws IOException {
                // Close DILI.
            }

            @Override
            public void write(Text key, AIMEDocument doc) throws IOException {
                // Write to DILI.
            }
        };
    }

    @Override
    public void checkOutputSpecs(FileSystem ignored, JobConf job) throws IOException {
    }
}
