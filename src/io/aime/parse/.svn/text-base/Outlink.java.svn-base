package io.aime.parse;

// Apache Hadoop
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

// IO
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

// Net
import java.net.MalformedURLException;

/**
 * An outgoing link from a page.
 *
 * @author Nutch.org
 * @author K-Zen
 */
public class Outlink implements Writable {

    private String toUrl = new String();
    private String anchor = new String();

    public Outlink() {
    }

    public Outlink(String toUrl, String anchor) throws MalformedURLException {
        this.toUrl = toUrl;

        if (anchor == null) {
            anchor = "";
        }

        this.anchor = anchor;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.toUrl = Text.readString(in);
        this.anchor = Text.readString(in);
    }

    public static void skip(DataInput in) throws IOException {
        Text.skip(in);
        Text.skip(in);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        Text.writeString(out, this.toUrl);
        Text.writeString(out, this.anchor);
    }

    public static Outlink read(DataInput in) throws IOException {
        Outlink outlink = new Outlink();
        outlink.readFields(in);

        return outlink;
    }

    public String getToUrl() {
        return this.toUrl;
    }

    public String getAnchor() {
        return this.anchor;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Outlink)) {
            return false;
        }

        Outlink other = (Outlink) o;

        return this.toUrl.equals(other.toUrl) && this.anchor.equals(other.anchor);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (this.toUrl != null ? this.toUrl.hashCode() : 0);
        hash = 61 * hash + (this.anchor != null ? this.anchor.hashCode() : 0);

        return hash;
    }

    @Override
    public String toString() {
        return "To Url: " + this.toUrl + ", Anchor: " + this.anchor;
    }
}
