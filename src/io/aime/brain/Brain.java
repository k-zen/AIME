package io.aime.brain;

import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.Metadata;
import io.aime.brain.data.SerializableData;
import io.aime.brain.xml.Handler;
import io.aime.util.Timer;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.ipc.RPC;
import org.apache.log4j.Logger;

/**
 * This is the central point where every single component of the applications
 * report changes, notifications, and informations.
 *
 * <p>
 * This class only executes in the JobTracker where the application is
 * running, not in the Nodes.
 * </p>
 *
 * <p>
 * All components report to this class via RPC using serializable objects.
 * For ex. in the case of Log4j logging events, they all get sent here via RPC
 * using LogHandler serializable objects, and they are de-serialized and added
 * to the LogHandler log queue, and showed in the LogWindow window of the
 * application.
 * </p>
 *
 * @author Andreas P. Koenzen <akc@apkc.net>
 * @see <a href="http://en.wikipedia.org/wiki/Singleton_pattern">Singleton Pattern</a>
 */
public class Brain implements BrainInterface
{

    private static final Logger LOG = Logger.getLogger(Brain.class.getName());
    private static final Brain _INSTANCE = new Brain();

    private Brain()
    {
    }

    public static Brain getInstance()
    {
        return _INSTANCE;
    }

    @Override
    public ObjectWritable execute(Text xml)
    {
        try {
            if (LOG.isTraceEnabled()) {
                LOG.trace("RAW XML ==> " + xml.toString());
            }

            BrainXMLData data = Handler.newBuild().getBrainXMLData(xml.toString());
            Object response = callMethod(data);

            return response == null ? new ObjectWritable() : new ObjectWritable(response);
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
            LOG.error("Error.", e);
            return new ObjectWritable();
        }
    }

    @Override
    public long getProtocolVersion(String protocol, long clientVersion) throws IOException
    {
        return VERSION;
    }

    @Override
    public boolean isAlive()
    {
        return true;
    }

    /**
     * Call a method from one of the meta data classes.
     *
     * @param data The XML data.
     *
     * @return The response.
     */
    private Object callMethod(BrainXMLData data) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException
    {
        if (SerializableData.class.isInstance(data.getClazz().newInstance())) {
            Class enclosingClass = data.getClazz().getEnclosingClass();
            final Object INSTANCE = enclosingClass.getMethod("getInstance").invoke(data.getClazz(), (Object[]) null); // Enclosing class Singleton instance.
            final Object METADATA_OBJECT = enclosingClass.cast(INSTANCE).getClass().getDeclaredMethod("read").invoke(INSTANCE, (Object[]) null); // Read the data object serialized to file.
            final Object DATA_OBJECT = enclosingClass.cast(INSTANCE).getClass().getDeclaredMethod("getData").invoke(METADATA_OBJECT, (Object[]) null); // Get the data object.

            switch (data.getJob()) {
                case BrainXMLData.JOB_REQUEST:
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Request Data: " + data.toString());
                    }

                    return data.getClazz().getDeclaredMethod("get" + data.getFunction()).invoke(DATA_OBJECT, (Object[]) null); // Execute
                case BrainXMLData.JOB_MERGE:
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Merge Data: " + data.toString());
                    }

                    final Object UPDATED_INSTANCE = data.getClazz().getDeclaredMethod("set" + data.getFunction(), new Class<?>[]{data.getParam().getType()}).invoke(DATA_OBJECT, new Object[]{data.getParam().getData()});
                    final Object UPDATED_METADATA_OBJECT = enclosingClass.cast(INSTANCE).getClass().getDeclaredMethod("setData", Object.class).invoke(INSTANCE, new Object[]{UPDATED_INSTANCE});

                    enclosingClass.cast(INSTANCE).getClass().getDeclaredMethod("merge", Metadata.class).invoke(INSTANCE, new Object[]{UPDATED_METADATA_OBJECT}); // Execute
            }
        }
        else {
            LOG.info("Passed object is not an instance of SerializableData!");
        }

        return null;
    }

    /**
     * Obtains a client to the Start.
     * <p>
     * This method waits X seconds for the Start to respond, if it
     * doesn't respond in a given time, then throw an exception.</p>
     *
     * @param conf Configuration's object.
     *
     * @return A Start client.
     */
    public static BrainInterface getClient(Configuration conf)
    {
        Timer t = new Timer();
        t.starTimer(); // Start timer.
        BrainInterface bean = null;
        int maxtries = 30;
        int tryCounter = 0;

        while (tryCounter < maxtries) {
            try {
                bean = (BrainInterface) RPC.getProxy(BrainInterface.class,
                                                     BrainInterface.VERSION,
                                                     new InetSocketAddress(conf.get("cerebellum.host"), conf.getInt("cerebellum.port", 14999)),
                                                     conf,
                                                     conf.getInt("rpc.timeout", 1));

                if (bean != null && bean.isAlive()) {
                    t.endTimer(); // Stop the timer.
                    return bean;
                }
            }
            catch (IOException e) {
                tryCounter++;
                LOG.info("Can't connect to Cerebellum. Try (" + tryCounter + "). Trying again...");
            }
        }

        LOG.fatal("Impossible to connect to Cerebellum. Giving up.");

        return bean;
    }
}
