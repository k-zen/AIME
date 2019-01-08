package io.aime.parse;

// Apache Hadoop
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VersionMismatchException;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;

// IO
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * The text conversion of page's content, stored using gzip compression.
 *
 * @author Nutch.org
 * @author K-Zen
 */
public final class ParseText implements Writable {

    public static final String DIR_NAME = "parse_text";
    private final static byte VERSION = 2;
    private String text = new String();

    public ParseText() {
    }

    public ParseText(String text) {
        this.text = text;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        byte version = in.readByte();
        switch (version) {
            case 1:
                this.text = WritableUtils.readCompressedString(in);
                break;
            case ParseText.VERSION:
                this.text = Text.readString(in);
                break;
            default:
                throw new VersionMismatchException(ParseText.VERSION, version);
        }
    }

    @Override
    public final void write(DataOutput out) throws IOException {
        out.write(ParseText.VERSION);
        Text.writeString(out, this.text);
    }

    public final static ParseText read(DataInput in) throws IOException {
        ParseText parseText = new ParseText();
        parseText.readFields(in);

        return parseText;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ParseText)) {
            return false;
        }
        ParseText other = (ParseText) o;

        return this.text.equals(other.text);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.text != null ? this.text.hashCode() : 0);

        return hash;
    }

    @Override
    public String toString() {
        return this.text;
    }
}
