package io.aime.fetcher;

import io.aime.crawl.AIMEWritable;
import io.aime.crawl.CrawlDatum;
import io.aime.parse.Parse;
import io.aime.parse.ParseOutputFormat;
import io.aime.protocol.Content;
import java.io.IOException;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.InvalidJobConfException;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.util.Progressable;

/**
 * Splits FetcherOutput entries into multiple map files.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class FetcherOutputFormat implements OutputFormat<Text, AIMEWritable>
{

    @Override
    public void checkOutputSpecs(FileSystem fs, JobConf job) throws IOException
    {
        Path out = FileOutputFormat.getOutputPath(job);

        // Check if the output dir is set.
        if ((out == null) && (job.getNumReduceTasks() != 0))
        {
            throw new InvalidJobConfException("Output directory not set in JobConf!");
        }

        // Check if the filesystem object is not null.
        if (fs == null)
        {
            fs = out.getFileSystem(job);
        }

        // If we enter here, that means this segment was fetched previously.
        if (fs.exists(new Path(out, CrawlDatum.FETCH_DIR_NAME)))
        {
            throw new IOException("Segment already fetched!");
        }
    }

    @Override
    public RecordWriter<Text, AIMEWritable> getRecordWriter(final FileSystem fs, final JobConf job, final String name, final Progressable progress) throws IOException
    {
        Path out = FileOutputFormat.getOutputPath(job);

        final Path fetch = new Path(new Path(out, CrawlDatum.FETCH_DIR_NAME), name);
        final Path content = new Path(new Path(out, Content.DIR_NAME), name);
        final CompressionType compType = SequenceFileOutputFormat.getOutputCompressionType(job);
        final MapFile.Writer fetchOut = new MapFile.Writer(job, fs, fetch.toString(), Text.class, CrawlDatum.class, compType, progress);

        return new RecordWriter<Text, AIMEWritable>()
        {
            private MapFile.Writer contentOut;
            private RecordWriter<Text, Parse> parseOut;

            
            {
                if (Fetcher.isStoringContent(job))
                {
                    contentOut = new MapFile.Writer(job, fs, content.toString(), Text.class, Content.class, compType, progress);
                }

                if (Fetcher.isParsing(job))
                {
                    parseOut = new ParseOutputFormat().getRecordWriter(fs, job, name, progress);
                }
            }

            @Override
            public void write(Text key, AIMEWritable value) throws IOException
            {
                Writable w = value.get();

                if (w instanceof CrawlDatum)
                {
                    fetchOut.append(key, w);
                }
                else if (w instanceof Content)
                {
                    contentOut.append(key, w);
                }
                else if (w instanceof Parse)
                {
                    parseOut.write(key, (Parse) w);
                }
            }

            @Override
            public void close(Reporter reporter) throws IOException
            {
                fetchOut.close();

                if (contentOut != null)
                {
                    contentOut.close();
                }

                if (parseOut != null)
                {
                    parseOut.close(reporter);
                }
            }
        };

    }
}
