package io.aime.brain.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * This serializable class will hold all metadata about a method available through the Cerebellum.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @see <a href="http://en.wikipedia.org/wiki/Builder_pattern">Builder Pattern</a>
 */
public class MetadataMethod extends Metadata
{

    /** The name of the method. */
    private String methodName = "";

    private MetadataMethod()
    {
    }

    public static MetadataMethod newBuild()
    {
        return new MetadataMethod();
    }

    public String getMethodName()
    {
        return methodName;
    }

    public MetadataMethod setMethodName(String methodName)
    {
        if (methodName != null && !methodName.isEmpty())
        {
            this.methodName = methodName;
        }

        return this;
    }

    @Override
    protected void updateInstance(Object newInstance)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Metadata setData(Object data)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getData()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getEmptyData()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void internalWrite(DataOutput out) throws IOException
    {
        out.writeUTF(methodName);
    }

    @Override
    public void internalRead(DataInput in) throws IOException
    {
        methodName = in.readUTF();
    }

    @Override
    public Metadata read()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void merge(Metadata newData)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
