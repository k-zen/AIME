package io.aime.brain.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.apache.hadoop.io.Writable;

/**
 * Class for describing metadata objects.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 */
public abstract class SerializableData implements Externalizable, Writable
{

    /**
     * Returns this instance.
     *
     * @return This instance.
     */
    public abstract SerializableData getData();

    @Override
    public final void writeExternal(ObjectOutput out) throws IOException
    {
        internalWrite(out);
    }

    @Override
    public final void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        internalRead(in);
    }

    @Override
    public final void write(DataOutput out) throws IOException
    {
        internalWrite(out);
    }

    @Override
    public final void readFields(DataInput in) throws IOException
    {
        internalRead(in);
    }

    /**
     * Writes this object to a stream.
     *
     * @param out The output stream.
     *
     * @throws IOException
     */
    protected abstract void internalWrite(DataOutput out) throws IOException;

    /**
     * Reads this object from a stream.
     *
     * @param in The input stream.
     *
     * @throws IOException
     */
    protected abstract void internalRead(DataInput in) throws IOException;
}
