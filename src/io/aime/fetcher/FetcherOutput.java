package io.aime.fetcher;

import io.aime.crawl.CrawlDatum;
import io.aime.parse.ParseImplementation;
import io.aime.protocol.Content;
import io.aime.util.AIMEConfiguration;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;

/**
 * An entry in the fetcher's output.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public final class FetcherOutput implements Writable, Configurable
{

    private Configuration conf;
    private CrawlDatum crawlDatum;
    private Content content;
    private ParseImplementation parse;

    @Override
    public final void setConf(Configuration conf)
    {
        if (conf == null)
        {
            this.conf = new AIMEConfiguration().create();
        }
        else
        {
            this.conf = conf;
        }
    }

    @Override
    public final Configuration getConf()
    {
        if (conf == null)
        {
            setConf(null);
        }

        return conf;
    }

    public FetcherOutput()
    {
    }

    public FetcherOutput(CrawlDatum crawlDatum, Content content, ParseImplementation parse)
    {
        this.crawlDatum = crawlDatum;
        this.content = content;
        this.parse = parse;
    }

    @Override
    public final void readFields(DataInput in) throws IOException
    {
        crawlDatum = CrawlDatum.read(in);
        content = in.readBoolean() ? Content.read(in) : null;
        parse = in.readBoolean() ? ParseImplementation.read(in) : null;
    }

    @Override
    public final void write(DataOutput out) throws IOException
    {
        crawlDatum.write(out);

        out.writeBoolean(content != null);
        if (content != null)
        {
            content.write(out);
        }

        out.writeBoolean(parse != null);
        if (parse != null)
        {
            parse.write(out);
        }
    }

    public CrawlDatum getCrawlDatum()
    {
        return crawlDatum;
    }

    public Content getContent()
    {
        return content;
    }

    public ParseImplementation getParse()
    {
        return parse;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof FetcherOutput))
        {
            return false;
        }

        FetcherOutput other = (FetcherOutput) o;

        return crawlDatum.equals(other.crawlDatum) && content.equals(other.content);
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 23 * hash + (crawlDatum != null ? crawlDatum.hashCode() : 0);
        hash = 23 * hash + (content != null ? content.hashCode() : 0);
        hash = 23 * hash + (parse != null ? parse.hashCode() : 0);

        return hash;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("CrawlDatum: ").append(crawlDatum).append("\n");

        return buffer.toString();
    }
}
