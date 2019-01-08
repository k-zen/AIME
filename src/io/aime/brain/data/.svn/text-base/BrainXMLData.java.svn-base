package io.aime.brain.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class BrainXMLData
{

    public static final byte JOB_REQUEST = 1;
    public static final byte JOB_MERGE = 2;
    public static final byte JOB_EXECUTE = 3;
    // ### DATA
    private byte job = -1;
    private Class clazz;
    private String function = "";
    private Parameter param = Parameter.newBuild();
    // ### DATA

    private BrainXMLData()
    {
    }

    public static BrainXMLData newBuild()
    {
        return new BrainXMLData();
    }

    // ### DATA FUNCTIONS
    public BrainXMLData setJob(byte job)
    {
        this.job = job;
        return this;
    }

    public byte getJob()
    {
        return job;
    }

    public BrainXMLData setClazz(Class clazz)
    {
        this.clazz = clazz;
        return this;
    }

    public Class getClazz()
    {
        return clazz;
    }

    public BrainXMLData setFunction(String function)
    {
        this.function = function;
        return this;
    }

    public String getFunction()
    {
        return function;
    }

    public BrainXMLData setParam(Parameter param)
    {
        this.param = param;
        return this;
    }

    public Parameter getParam()
    {
        return param;
    }
    // ### DATA FUNCTIONS

    // ### UTILITIES
    @Override
    public String toString()
    {
        StringBuilder b = new StringBuilder();
        b.append("\n");
        b.append("Class: ").append(clazz.getName()).append("\n");
        b.append("Funtion: ").append(function).append("\n");
        b.append("Parameter: ").append(param.getType().getName()).append(" [").append(param.getData().toString()).append("]").append("\n");

        return b.toString();
    }
    // ### UTILITIES

    public static class Parameter
    {

        private Class type = Object.class;
        private byte[] data = new byte[0];

        private Parameter()
        {
        }

        public static Parameter newBuild()
        {
            return new Parameter();
        }

        public Parameter setType(Class type)
        {
            this.type = type;
            return this;
        }

        public Class getType()
        {
            return type;
        }

        public Parameter setData(Object data)
        {
            ByteArrayOutputStream bos = null;
            ObjectOutputStream oos = null;
            try
            {
                bos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(bos);

                oos.writeObject(data);

                this.data = bos.toByteArray();
            }
            catch (Exception e)
            {
                this.data = new byte[0];
            }
            finally
            {
                if (bos != null)
                {
                    try
                    {
                        bos.close();
                    }
                    catch (Exception e)
                    {
                        // #TODO: Improove.
                    }
                }

                if (oos != null)
                {
                    try
                    {
                        oos.close();
                    }
                    catch (Exception e)
                    {
                        // #TODO: Improove.
                    }
                }
            }

            return this;
        }

        public Parameter setDataAsByteArray(byte[] data)
        {
            this.data = data;
            return this;
        }

        public Object getData()
        {
            ByteArrayInputStream bis = null;
            ObjectInputStream ois = null;
            try
            {
                bis = new ByteArrayInputStream(data);
                ois = new ObjectInputStream(bis);

                return type.cast(ois.readObject());
            }
            catch (IOException | ClassNotFoundException e)
            {
                return new Object();
            }
            finally
            {
                if (bis != null)
                {
                    try
                    {
                        bis.close();
                    }
                    catch (Exception e)
                    {
                        // #TODO: Improove.
                    }
                }

                if (ois != null)
                {
                    try
                    {
                        ois.close();
                    }
                    catch (Exception e)
                    {
                        // #TODO: Improove.
                    }
                }
            }
        }

        public byte[] getDataAsByteArray()
        {
            return data;
        }
    }
}
