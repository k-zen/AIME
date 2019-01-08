package io.aime.parse;

import io.aime.aimemisc.datamining.Block;
import io.aime.metadata.DocMetadata;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VersionMismatchException;
import org.apache.hadoop.io.VersionedWritable;

/**
 * This class represents the data that was extracted from a given document.
 *
 * <p>
 * In this case data represents informations like:
 * <ul>
 * <li>The server holding the document.</li>
 * <li>Title of the page</li>
 * <li>Facebook comments</li>
 * <li>Outlinks in the page</li>
 * <li>etc</li>
 * </ul>
 * </p>
 *
 * @author Nutch.org
 * @author K-Zen
 */
public final class ParseData extends VersionedWritable
{

    public static final String DIR_NAME = "parse_data";
    private final static byte VERSION = 5;
    private String title = new String();
    private Block[] contentBlocks = new Block[0];
    private Outlink[] outlinks = new Outlink[0];
    private DocMetadata contentMeta = new DocMetadata();
    private DocMetadata parseMeta = new DocMetadata();
    private ParseStatus status = new ParseStatus();
    private byte version = ParseData.VERSION;

    public ParseData()
    {
    }

    public ParseData(ParseStatus status, String title, Outlink[] outlinks, DocMetadata contentMeta)
    {
        this(status, title, outlinks, contentMeta, new DocMetadata());
    }

    public ParseData(ParseStatus status, String title, Outlink[] outlinks, DocMetadata contentMeta, DocMetadata parseMeta)
    {
        this.status = status;
        this.title = title;
        this.outlinks = outlinks;
        this.contentMeta = contentMeta;
        this.parseMeta = parseMeta;
    }

    public ParseData(ParseStatus status, String title, Outlink[] outlinks, DocMetadata contentMeta, DocMetadata parseMeta, Block[] contentBlocks)
    {
        this.status = status;
        this.title = title;
        this.outlinks = outlinks;
        this.contentMeta = contentMeta;
        this.parseMeta = parseMeta;
        this.contentBlocks = contentBlocks;
    }

    public ParseStatus getStatus()
    {
        return this.status;
    }

    public String getTitle()
    {
        return this.title;
    }

    public Block[] getContentBlocks()
    {
        return this.contentBlocks;
    }

    public Outlink[] getOutlinks()
    {
        return this.outlinks;
    }

    public DocMetadata getContentMeta()
    {
        return this.contentMeta;
    }

    public DocMetadata getParseMeta()
    {
        return parseMeta;
    }

    public void setParseMeta(DocMetadata parseMeta)
    {
        this.parseMeta = parseMeta;
    }

    public String getMeta(String name)
    {
        String value = parseMeta.get(name);

        if (value == null)
        {
            value = contentMeta.get(name);
        }

        return value;
    }

    @Override
    public byte getVersion()
    {
        return version;
    }

    @Override
    public final void readFields(DataInput in) throws IOException
    {
        this.version = in.readByte();

        if (this.version != ParseData.VERSION)
        {
            throw new VersionMismatchException(ParseData.VERSION, this.version);
        }

        this.status = ParseStatus.read(in);
        this.title = Text.readString(in);

        int numBlocks = in.readInt();
        this.contentBlocks = new Block[numBlocks];
        for (int k = 0; k < numBlocks; k++)
        {
            this.contentBlocks[k] = Block.read(in);
        }

        int numOutlinks = in.readInt();
        this.outlinks = new Outlink[numOutlinks];
        for (int i = 0; i < numOutlinks; i++)
        {
            this.outlinks[i] = Outlink.read(in);
        }

        if (this.version < 3)
        {
            int propertyCount = in.readInt();
            this.contentMeta.clear();

            for (int i = 0; i < propertyCount; i++)
            {
                this.contentMeta.add(Text.readString(in), Text.readString(in));
            }
        }
        else
        {
            this.contentMeta.clear();
            this.contentMeta.readFields(in);
        }

        if (this.version > 3)
        {
            this.parseMeta.clear();
            this.parseMeta.readFields(in);
        }
    }

    @Override
    public final void write(DataOutput out) throws IOException
    {
        out.writeByte(ParseData.VERSION);
        this.status.write(out);
        Text.writeString(out, this.title);

        out.writeInt(this.contentBlocks.length);
        for (Block contentBlock : this.contentBlocks)
        {
            contentBlock.write(out);
        }

        out.writeInt(this.outlinks.length);
        for (Outlink outlink : this.outlinks)
        {
            outlink.write(out);
        }

        this.contentMeta.write(out);
        this.parseMeta.write(out);
    }

    public static ParseData read(DataInput in) throws IOException
    {
        ParseData parseText = new ParseData();
        parseText.readFields(in);

        return parseText;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof ParseData))
        {
            return false;
        }

        ParseData other = (ParseData) o;

        return this.status.equals(other.status)
                && this.title.equals(other.title)
                && Arrays.equals(this.contentBlocks, other.contentBlocks)
                && Arrays.equals(this.outlinks, other.outlinks)
                && this.contentMeta.equals(other.contentMeta)
                && this.parseMeta.equals(other.parseMeta);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + (this.title != null ? this.title.hashCode() : 0);
        hash = 53 * hash + Arrays.deepHashCode(this.contentBlocks);
        hash = 53 * hash + Arrays.deepHashCode(this.outlinks);
        hash = 53 * hash + (this.contentMeta != null ? this.contentMeta.hashCode() : 0);
        hash = 53 * hash + (this.parseMeta != null ? this.parseMeta.hashCode() : 0);
        hash = 53 * hash + (this.status != null ? this.status.hashCode() : 0);
        hash = 53 * hash + this.version;

        return hash;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Version: ").append(this.version).append("\n");
        buffer.append("Status: ").append(this.status).append("\n");
        buffer.append("Title: ").append(this.title).append("\n");

        if (this.contentBlocks != null)
        {
            buffer.append("Content Blocks: ").append(this.contentBlocks.length).append("\n");
            for (Block contentBlock : this.contentBlocks)
            {
                buffer.append(contentBlock.toString());
            }
        }

        if (this.outlinks != null)
        {
            buffer.append("Outlinks: ").append(this.outlinks.length).append("\n");
            for (Outlink outlink : this.outlinks)
            {
                buffer.append(" Outlink: ").append(outlink.toString()).append("\n");
            }
        }

        buffer.append("Content Metadata: ").append(this.contentMeta).append("\n");
        buffer.append("Parse Metadata: ").append(this.parseMeta).append("\n");

        return buffer.toString();
    }
}
