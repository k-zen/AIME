package io.aime.crawl;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.hadoop.io.Writable;

/**
 * A list of {@link Inlink}s.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class Inlinks implements Writable
{

    private HashSet<Inlink> inlinks = new HashSet<>(1);

    public void add(Inlink inlink)
    {
        inlinks.add(inlink);
    }

    public void add(Inlinks inlinks)
    {
        this.inlinks.addAll(inlinks.inlinks);
    }

    public Iterator<Inlink> iterator()
    {
        return inlinks.iterator();
    }

    public int size()
    {
        return inlinks.size();
    }

    public void clear()
    {
        inlinks.clear();
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        int length = in.readInt();
        inlinks.clear();

        for (int i = 0; i < length; i++)
        {
            add(Inlink.read(in));
        }
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeInt(inlinks.size());
        Iterator<Inlink> it = inlinks.iterator();

        while (it.hasNext())
        {
            it.next().write(out);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Inlinks:\n");
        Iterator<Inlink> it = inlinks.iterator();

        while (it.hasNext())
        {
            buffer.append(" ");
            buffer.append(it.next());
            buffer.append("\n");
        }

        return buffer.toString();
    }
}
