package io.aime.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;

/**
 * Utility class for counting time.
 *
 * @author K-Zen
 */
public class Timer implements Writable
{

    public static enum Time
    {

        MILLISECOND, SECOND, MINUTE, HOUR
    }
    private long start = 0L;
    private long end = 0L;

    public void starTimer()
    {
        start = System.currentTimeMillis();
    }

    public void endTimer()
    {
        end = System.currentTimeMillis();
    }

    /**
     * Returns the running time in milliseconds.
     *
     * @return The running time.
     */
    public long getExecutionTime()
    {
        if (start > 0L) {
            return (System.currentTimeMillis() - start);
        }
        else {
            return 0L;
        }
    }

    /**
     * This method will compute the difference between two times in
     * milliseconds.
     *
     * @param timeUnit The time unit for the response.
     *
     * @return The time difference.
     */
    public double computeOperationTime(Timer.Time timeUnit)
    {
        switch (timeUnit) {
            case MILLISECOND:
                return (end - start);
            case SECOND:
                return ((end - start) / 1000.0);
            case MINUTE:
                return ((end - start) / 1000.0) / 60;
            case HOUR:
                return ((end - start) / 1000.0) / 3600;
            default:
                return (end - start);
        }
    }

    /**
     * This method will compute the difference between two times, but the time
     * is passed as a parameter.
     *
     * @param timeUnit    The time unit for the response.
     * @param elapsedTime The time in milliseconds.
     *
     * @return The time difference.
     */
    public double customComputeOperationTime(Timer.Time timeUnit, long elapsedTime)
    {
        switch (timeUnit) {
            case MILLISECOND:
                return (elapsedTime);
            case SECOND:
                return (elapsedTime / 1000.0);
            case MINUTE:
                return (elapsedTime / 1000.0) / 60;
            case HOUR:
                return (elapsedTime / 1000.0) / 3600;
            default:
                return (elapsedTime);
        }
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeLong(start);
        out.writeLong(end);
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        start = in.readLong();
        end = in.readLong();
    }

    public static Timer read(DataInput in) throws IOException
    {
        Timer t = new Timer();
        t.readFields(in);

        return t;
    }
}
