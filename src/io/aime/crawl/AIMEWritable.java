package io.aime.crawl;

import io.aime.fetcher.FetcherOutput;
import io.aime.metadata.DocMetadata;
import io.aime.parse.Outlink;
import io.aime.parse.ParseData;
import io.aime.parse.ParseImplementation;
import io.aime.parse.ParseStatus;
import io.aime.parse.ParseText;
import io.aime.protocol.Content;
import io.aime.protocol.ProtocolStatus;
import io.aime.util.GenericWritableConfigurable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

/**
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class AIMEWritable extends GenericWritableConfigurable
{

    private static Class<? extends Writable>[] CLASSES = null;

    static
    {
        CLASSES = new Class[]
        {
            BytesWritable.class,
            FloatWritable.class,
            IntWritable.class,
            LongWritable.class,
            MapWritable.class,
            MD5Hash.class,
            NullWritable.class,
            Text.class,
            CrawlDatum.class,
            Inlink.class,
            Inlinks.class,
            FetcherOutput.class,
            DocMetadata.class,
            Outlink.class,
            ParseText.class,
            ParseData.class,
            ParseImplementation.class,
            ParseStatus.class,
            Content.class,
            ProtocolStatus.class
        };
    }

    public AIMEWritable()
    {
    }

    public AIMEWritable(Writable instance)
    {
        super.set(instance);
    }

    @Override
    protected Class<? extends Writable>[] getTypes()
    {
        return CLASSES;
    }
}
