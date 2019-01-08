package io.aime.parse;

// Apache Hadoop
import org.apache.hadoop.io.*;

// IO
import java.io.*;

/**
 * The result of parsing a page's raw content.
 * @see Parser#getParse(Content)
 */
public class ParseImplementation implements Parse, Writable {

    private ParseText text;
    private ParseData data;
    private boolean isCanonical;

    public ParseImplementation() {
    }

    public ParseImplementation(Parse parse) {
        this(new ParseText(parse.getText()), parse.getData(), true);
    }

    public ParseImplementation(String text, ParseData data) {
        this(new ParseText(text), data, true);
    }

    public ParseImplementation(ParseText text, ParseData data) {
        this(text, data, true);
    }

    public ParseImplementation(ParseText text, ParseData data, boolean isCanonical) {
        this.text = text;
        this.data = data;
        this.isCanonical = isCanonical;
    }

    @Override
    public String getText() {
        return text.getText();
    }

    @Override
    public ParseData getData() {
        return data;
    }

    @Override
    public boolean isCanonical() {
        return isCanonical;
    }

    @Override
    public final void write(DataOutput out) throws IOException {
        out.writeBoolean(isCanonical);
        text.write(out);
        data.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        isCanonical = in.readBoolean();
        text = new ParseText();
        text.readFields(in);
        data = new ParseData();
        data.readFields(in);
    }

    public static ParseImplementation read(DataInput in) throws IOException {
        ParseImplementation parseImpl = new ParseImplementation();
        parseImpl.readFields(in);

        return parseImpl;
    }
}
