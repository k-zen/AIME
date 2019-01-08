package io.aime.brain.data;

import io.aime.aimemisc.io.FileStoring;
import io.aime.bot.ConsoleMessage;
import io.aime.util.AIMEConstants;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;

/**
 * This serializable class will hold all metadata about the Console.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @see <a href="http://en.wikipedia.org/wiki/Singleton_pattern">Singleton Pattern</a>
 */
public class MetadataConsole extends Metadata
{

    private static final Logger LOG = Logger.getLogger(MetadataConsole.class.getName());
    private static volatile MetadataConsole _INSTANCE = new MetadataConsole();
    /** Mark if this instance is empty. If TRUE then we must load data from file. */
    private static volatile boolean isEmpty = true;
    /** Empty data object. */
    private Data data = new Data();

    public static MetadataConsole getInstance()
    {
        return _INSTANCE;
    }

    @Override
    protected void updateInstance(Object newInstance)
    {
        if (newInstance != null) {
            _INSTANCE = (MetadataConsole) newInstance;
        }
    }

    @Override
    public MetadataConsole setData(Object data)
    {
        this.data = (Data) data;
        return this;
    }

    @Override
    public MetadataConsole.Data getData()
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
    public MetadataConsole read()
    {
        if (isEmpty) {
            File f = new File(AIMEConstants.DEFAULT_JOB_DATA_FOLDER.getStringConstant() + "/" + AIMEConstants.METADATA_CONSOLE_FILENAME.getStringConstant());
            getInstance().updateInstance((MetadataConsole) FileStoring.getInstance().readFromFile(
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
                new File(AIMEConstants.DEFAULT_JOB_DATA_FOLDER.getStringConstant() + "/" + AIMEConstants.METADATA_CONSOLE_FILENAME.getStringConstant()),
                (MetadataConsole) newData,
                AIMEConstants.METADATA_ENCRYPT.getBooleanConstant(),
                AIMEConstants.METADATA_KEY.getStringConstant(),
                AIMEConstants.METADATA_ENCODING.getStringConstant());
    }

    public static class Data extends SerializableData
    {

        public static final MetadataMethod MESSAGE = MetadataMethod.newBuild().setMethodName("Message");
        public static final MetadataMethod DEPTH = MetadataMethod.newBuild().setMethodName("Depth");
        public static final MetadataMethod ALL_MESSAGES = MetadataMethod.newBuild().setMethodName("AllMessages");
        // ### DATA
        private Queue<ConsoleMessage> botMsgs = new ConcurrentLinkedQueue<>();
        // ### DATA

        public static Data newBuild()
        {
            return new Data();
        }

        @Override
        public MetadataConsole.Data getData()
        {
            return this;
        }

        // ### DATA FUNCTIONS
        /**
         * Inserts a new message into the queue.
         * <p>
         * The queue used here is a FIFO queue.</p>
         * <p>
         * This method synchronizes on the instance.</p>
         *
         * @param message The message to insert.
         *
         * @return The only instance of the class.
         */
        public synchronized Data setMessage(Object message)
        {
            if (!(message instanceof ConsoleMessage)) {
                LOG.warn("Wrong object passed to BotConsole.");
                return this;
            }

            botMsgs.add((ConsoleMessage) message);
            return this;
        }

        /**
         * Retrieves a message from the queue.
         * <p>
         * The queue used here is a FIFO queue.</p>
         * <p>
         * Since the collection is a queue, then the message at the head of the
         * queue should be the one returned.</p>
         *
         * @return The message at the head of the queue.
         */
        public synchronized ConsoleMessage getMessage()
        {
            return (!botMsgs.isEmpty()) ? botMsgs.poll() : ConsoleMessage.newBuild();
        }

        /**
         * Return how many messages are in the queue.
         *
         * @return The number of messages in the queue.
         */
        public int getDepth()
        {
            return botMsgs.size();
        }

        /**
         * Method for returning all messages in the queue at once.
         *
         * @return An array with all messages in the queue.
         */
        public synchronized ConsoleMessage[] getAllMessages()
        {
            ConsoleMessage[] queue = (!botMsgs.isEmpty()) ? botMsgs.toArray(new ConsoleMessage[0]) : new ConsoleMessage[0];
            // Empty queue.
            botMsgs.clear();

            return queue;
        }
        // ### DATA FUNCTIONS

        // ### SERIALIZATION FUNCTIONS
        @Override
        protected void internalWrite(DataOutput out) throws IOException
        {
            out.writeInt(botMsgs.size());
            for (ConsoleMessage m : botMsgs) {
                m.write(out);
            }
        }

        @Override
        protected void internalRead(DataInput in) throws IOException
        {
            int length = in.readInt();
            botMsgs = new ConcurrentLinkedQueue<>();
            for (int k = 0; k < length; k++) {
                ConsoleMessage m = ConsoleMessage.read(in);
                botMsgs.add(m);
            }
        }
        // ### SERIALIZATION FUNCTIONS
    }
}
