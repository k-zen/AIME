package io.aime.brain.data;

import io.aime.aimemisc.io.FileStoring;
import io.aime.util.AIMEConstants;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 * This serializable class will hold all metadata about the Fetcher.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @see <a href="http://en.wikipedia.org/wiki/Singleton_pattern">Singleton Pattern</a>
 */
public class MetadataFetcher extends Metadata
{

    private static final Logger LOG = Logger.getLogger(MetadataFetcher.class.getName());
    private static volatile MetadataFetcher _INSTANCE = new MetadataFetcher();
    /** Mark if this instance is empty. If TRUE then we must load data from file. */
    private static volatile boolean isEmpty = true;
    /** Empty data object. */
    private Data data = new Data();

    public static MetadataFetcher getInstance()
    {
        return _INSTANCE;
    }

    @Override
    protected void updateInstance(Object newInstance)
    {
        if (newInstance != null) {
            _INSTANCE = (MetadataFetcher) newInstance;
        }
    }

    @Override
    public MetadataFetcher setData(Object data)
    {
        this.data = (Data) data;
        return this;
    }

    @Override
    public MetadataFetcher.Data getData()
    {
        return data;
    }

    @Override
    public Object getEmptyData()
    {
        return new Data();
    }

    @Override
    public void internalWrite(DataOutput out) throws IOException
    {
        data.write(out);
    }

    @Override
    public void internalRead(DataInput in) throws IOException
    {
        Data d = new Data();
        d.internalRead(in);
        data = d;
    }

    @Override
    public MetadataFetcher read()
    {
        if (isEmpty) {
            File f = new File(AIMEConstants.DEFAULT_JOB_DATA_FOLDER.getStringConstant() + "/" + AIMEConstants.METADATA_FETCHER_FILENAME.getStringConstant());
            getInstance().updateInstance((MetadataFetcher) FileStoring.getInstance().readFromFile(
                    f,
                    AIMEConstants.METADATA_ENCRYPT.getBooleanConstant(),
                    AIMEConstants.METADATA_KEY.getStringConstant(),
                    AIMEConstants.METADATA_ENCODING.getStringConstant()));
            isEmpty = false;
        }

        return getInstance();
    }

    @Override
    public void merge(Metadata newData)
    {
        FileStoring.getInstance().writeToFile(
                new File(AIMEConstants.DEFAULT_JOB_DATA_FOLDER.getStringConstant() + "/" + AIMEConstants.METADATA_FETCHER_FILENAME.getStringConstant()),
                (MetadataFetcher) newData,
                AIMEConstants.METADATA_ENCRYPT.getBooleanConstant(),
                AIMEConstants.METADATA_KEY.getStringConstant(),
                AIMEConstants.METADATA_ENCODING.getStringConstant());
    }

    public static class Data extends SerializableData
    {

        public static final MetadataMethod SUCCESS = MetadataMethod.newBuild().setMethodName("Success");
        public static final MetadataMethod FAILED = MetadataMethod.newBuild().setMethodName("Failed");
        public static final MetadataMethod GONE = MetadataMethod.newBuild().setMethodName("Gone");
        public static final MetadataMethod MOVED = MetadataMethod.newBuild().setMethodName("Moved");
        public static final MetadataMethod TEMP_MOVED = MetadataMethod.newBuild().setMethodName("TempMoved");
        public static final MetadataMethod NOT_FOUND = MetadataMethod.newBuild().setMethodName("NotFound");
        public static final MetadataMethod RETRY = MetadataMethod.newBuild().setMethodName("Retry");
        public static final MetadataMethod EXCEPTION = MetadataMethod.newBuild().setMethodName("Exception");
        public static final MetadataMethod ACCESS_DENIED = MetadataMethod.newBuild().setMethodName("AccessDenied");
        public static final MetadataMethod ROBOTS_DENIED = MetadataMethod.newBuild().setMethodName("RobotsDenied");
        public static final MetadataMethod REDIR_EXCEEDED = MetadataMethod.newBuild().setMethodName("RedirExceeded");
        public static final MetadataMethod NOT_MODIFIED = MetadataMethod.newBuild().setMethodName("NotModified");
        public static final MetadataMethod WOULD_BLOCK = MetadataMethod.newBuild().setMethodName("WouldBlock");
        public static final MetadataMethod BLOCKED = MetadataMethod.newBuild().setMethodName("Blocked");
        public static final MetadataMethod UNKNOWN = MetadataMethod.newBuild().setMethodName("Unknown");
        public static final MetadataMethod ACTIVE_THREADS = MetadataMethod.newBuild().setMethodName("ActiveThreads");
        public static final MetadataMethod WAITING_THREADS = MetadataMethod.newBuild().setMethodName("WaitingThreads");
        public static final MetadataMethod QUEUE_SIZE = MetadataMethod.newBuild().setMethodName("QueueSize");
        public static final MetadataMethod PAGES = MetadataMethod.newBuild().setMethodName("Pages");
        public static final MetadataMethod ERRORS = MetadataMethod.newBuild().setMethodName("Errors");
        public static final MetadataMethod ELAPSED = MetadataMethod.newBuild().setMethodName("Elapsed");
        public static final MetadataMethod BYTES = MetadataMethod.newBuild().setMethodName("Bytes");
        // ### DATA
        // Status Table
        private int success = 0;
        private int failed = 0;
        private int gone = 0;
        private int moved = 0;
        private int tempMoved = 0;
        private int notFound = 0;
        private int retry = 0;
        private int exception = 0;
        private int accessDenied = 0;
        private int robotsDenied = 0;
        private int redirExceeded = 0;
        private int notModified = 0;
        private int wouldBlock = 0;
        private int blocked = 0;
        private int unknown = 0;
        // Stats Table
        private long activeThreads = 0L;
        private long waitingThreads = 0L;
        private long queueSize = 0L;
        private long pages = 0L;
        private long errors = 0L;
        private long elapsed = 0L;
        private long bytes = 0L;
        // ### DATA

        public static Data newBuild()
        {
            return new Data();
        }

        @Override
        public MetadataFetcher.Data getData()
        {
            return this;
        }

        // ### DATA FUNCTIONS
        public Data setSuccess(int success)
        {
            this.success = success;
            return this;
        }

        public int getSuccess()
        {
            return success;
        }

        public Data setFailed(int failed)
        {
            this.failed = failed;
            return this;
        }

        public int getFailed()
        {
            return failed;
        }

        public Data setGone(int gone)
        {
            this.gone = gone;
            return this;
        }

        public int getGone()
        {
            return gone;
        }

        public Data setMoved(int moved)
        {
            this.moved = moved;
            return this;
        }

        public int getMoved()
        {
            return moved;
        }

        public Data setTempMoved(int tempMoved)
        {
            this.tempMoved = tempMoved;
            return this;
        }

        public int getTempMoved()
        {
            return tempMoved;
        }

        public Data setNotFound(int notFound)
        {
            this.notFound = notFound;
            return this;
        }

        public int getNotFound()
        {
            return notFound;
        }

        public Data setRetry(int retry)
        {
            this.retry = retry;
            return this;
        }

        public int getRetry()
        {
            return retry;
        }

        public Data setException(int exception)
        {
            this.exception = exception;
            return this;
        }

        public int getException()
        {
            return exception;
        }

        public Data setAccessDenied(int accessDenied)
        {
            this.accessDenied = accessDenied;
            return this;
        }

        public int getAccessDenied()
        {
            return accessDenied;
        }

        public Data setRobotsDenied(int robotsDenied)
        {
            this.robotsDenied = robotsDenied;
            return this;
        }

        public int getRobotsDenied()
        {
            return robotsDenied;
        }

        public Data setRedirExceeded(int redirExceeded)
        {
            this.redirExceeded = redirExceeded;
            return this;
        }

        public int getRedirExceeded()
        {
            return redirExceeded;
        }

        public Data setNotModified(int notModified)
        {
            this.notModified = notModified;
            return this;
        }

        public int getNotModified()
        {
            return notModified;
        }

        public Data setWouldBlock(int wouldBlock)
        {
            this.wouldBlock = wouldBlock;
            return this;
        }

        public int getWouldBlock()
        {
            return wouldBlock;
        }

        public Data setBlocked(int blocked)
        {
            this.blocked = blocked;
            return this;
        }

        public int getBlocked()
        {
            return blocked;
        }

        public Data setUnknown(int unknown)
        {
            this.unknown = unknown;
            return this;
        }

        public int getUnknown()
        {
            return unknown;
        }

        public Data setActiveThreads(long activeThreads)
        {
            this.activeThreads = activeThreads;
            return this;
        }

        public long getActiveThreads()
        {
            return activeThreads;
        }

        public Data setWaitingThreads(long waitingThreads)
        {
            this.waitingThreads = waitingThreads;
            return this;
        }

        public long getWaitingThreads()
        {
            return waitingThreads;
        }

        public Data setQueueSize(long queueSize)
        {
            this.queueSize = queueSize;
            return this;
        }

        public long getQueueSize()
        {
            return queueSize;
        }

        public Data setPages(long pages)
        {
            this.pages = pages;
            return this;
        }

        public long getPages()
        {
            return pages;
        }

        public Data setErrors(long errors)
        {
            this.errors = errors;
            return this;
        }

        public long getErrors()
        {
            return errors;
        }

        public Data setElapsed(long elapsed)
        {
            this.elapsed = elapsed;
            return this;
        }

        public long getElapsed()
        {
            return elapsed;
        }

        public Data setBytes(long bytes)
        {
            this.bytes = bytes;
            return this;
        }

        public long getBytes()
        {
            return bytes;
        }
        // ### DATA FUNCTIONS

        // ### SERIALIZATION FUNCTIONS
        @Override
        protected void internalWrite(DataOutput out) throws IOException
        {
            // Integer variables
            out.writeInt(success);
            out.writeInt(failed);
            out.writeInt(gone);
            out.writeInt(moved);
            out.writeInt(tempMoved);
            out.writeInt(notFound);
            out.writeInt(retry);
            out.writeInt(exception);
            out.writeInt(accessDenied);
            out.writeInt(robotsDenied);
            out.writeInt(redirExceeded);
            out.writeInt(notModified);
            out.writeInt(wouldBlock);
            out.writeInt(blocked);
            out.writeInt(unknown);
            // Long variables
            out.writeLong(activeThreads);
            out.writeLong(waitingThreads);
            out.writeLong(queueSize);
            out.writeLong(pages);
            out.writeLong(errors);
            out.writeLong(elapsed);
            out.writeLong(bytes);
        }

        @Override
        protected void internalRead(DataInput in) throws IOException
        {
            // Integer variables
            success = in.readInt();
            failed = in.readInt();
            gone = in.readInt();
            moved = in.readInt();
            tempMoved = in.readInt();
            notFound = in.readInt();
            retry = in.readInt();
            exception = in.readInt();
            accessDenied = in.readInt();
            robotsDenied = in.readInt();
            redirExceeded = in.readInt();
            notModified = in.readInt();
            wouldBlock = in.readInt();
            blocked = in.readInt();
            unknown = in.readInt();
            // Long variables
            activeThreads = in.readLong();
            waitingThreads = in.readLong();
            queueSize = in.readLong();
            pages = in.readLong();
            errors = in.readLong();
            elapsed = in.readLong();
            bytes = in.readLong();
        }
    }
    // ### SERIALIZATION FUNCTIONS
}
