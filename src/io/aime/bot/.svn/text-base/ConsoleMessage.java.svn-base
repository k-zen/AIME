package io.aime.bot;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.apache.hadoop.io.Writable;

/**
 * @author Andreas P. Koenzen <akc@apkc.net>
 * @version 0.2
 */
public final class ConsoleMessage implements Externalizable, Writable
{

    public static final int INFO = 1;
    public static final int WARN = 2;
    public static final int ERROR = 3;
    private int severity = 1;
    private String message = "";

    public ConsoleMessage()
    {
    }

    public static ConsoleMessage newBuild()
    {
        return new ConsoleMessage();
    }

    public ConsoleMessage setSeverity(int severity)
    {
        this.severity = severity;
        return this;
    }

    public ConsoleMessage setMessage(String message)
    {
        this.message = message;
        return this;
    }

    public int getSeverity()
    {
        return severity;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        internalWrite(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        internalRead(in);
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        internalWrite(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        internalRead(in);
    }

    private void internalWrite(DataOutput out) throws IOException
    {
        out.writeInt(severity);
        out.writeUTF(message);
    }

    private void internalRead(DataInput in) throws IOException
    {
        severity = in.readInt();
        message = in.readUTF();
    }

    public static ConsoleMessage read(DataInput in) throws IOException
    {
        ConsoleMessage m = ConsoleMessage.newBuild();
        m.internalRead(in);

        return m;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof ConsoleMessage))
        {
            return false;
        }

        ConsoleMessage other = (ConsoleMessage) o;

        return (severity == other.severity) && (message.equals(other.message));
    }

    @Override
    public int hashCode()
    {
        int res = 0;
        res ^= severity;
        res ^= message.hashCode();

        return res;
    }

    /**
     * Checks if this message is empty.
     *
     * @return TRUE if the message is empty, FALSE otherwise.
     */
    public boolean isEmpty()
    {
        return (severity == 1 && message.isEmpty());
    }
}
