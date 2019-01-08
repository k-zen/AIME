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
public abstract class Metadata implements Externalizable, Writable
{

    /**
     * Method for updating an instance.
     *
     * @param newInstance The new instance.
     */
    protected abstract void updateInstance(Object newInstance);

    /**
     * Set the data object.
     *
     * @param data The data object.
     *
     * @return This instance.
     */
    public abstract Metadata setData(Object data);

    /**
     * Gets the data object.
     *
     * @return The data object.
     */
    public abstract Object getData();

    /**
     * Returns a clear and new data object.
     *
     * @return A new data object.
     */
    public abstract Object getEmptyData();

    /**
     * Reads a metadata object and updates the instance.
     *
     * @return The metadata object.
     */
    public abstract Metadata read();

    /**
     * Updates a metadata object.
     *
     * @param newData The new data.
     */
    public abstract void merge(Metadata newData);

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
    public abstract void internalWrite(DataOutput out) throws IOException;

    /**
     * Reads this object from a stream.
     *
     * @param in The input stream.
     *
     * @throws IOException
     */
    public abstract void internalRead(DataInput in) throws IOException;
}
