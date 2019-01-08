package io.aime.crawl;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

/**
 * An incoming link to a page.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class Inlink implements Writable
{

    private String fromURL;
    private String anchor;

    public Inlink()
    {
    }

    public Inlink(String fromURL, String anchor)
    {
        this.fromURL = fromURL;
        this.anchor = anchor;
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        fromURL = Text.readString(in);
        anchor = Text.readString(in);
    }

    public static void skip(DataInput in) throws IOException
    {
        Text.skip(in); // skip fromUrl
        Text.skip(in); // skip anchor
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        Text.writeString(out, fromURL);
        Text.writeString(out, anchor);
    }

    public static Inlink read(DataInput in) throws IOException
    {
        Inlink inlink = new Inlink();
        inlink.readFields(in);
        return inlink;
    }

    public String getFromURL()
    {
        return fromURL;
    }

    public String getAnchor()
    {
        return anchor;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Inlink))
        {
            return false;
        }

        Inlink other = (Inlink) o;

        return fromURL.equals(other.fromURL) && anchor.equals(other.anchor);
    }

    @Override
    public int hashCode()
    {
        return fromURL.hashCode() ^ anchor.hashCode();
    }

    @Override
    public String toString()
    {
        return "fromUrl: " + fromURL + " anchor: " + anchor;
    }
}
