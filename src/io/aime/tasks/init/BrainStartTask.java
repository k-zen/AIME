package io.aime.tasks.init;

import io.aime.brain.Brain;
import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.BrainXMLData.Parameter;
import io.aime.brain.data.MetadataGeneral;
import io.aime.brain.xml.Handler;
import io.aime.net.URLFilter;
import io.aime.net.URLFilters;
import io.aime.util.AIMEConfiguration;
import io.aime.util.AIMEConstants;
import io.aime.util.LogEventHandler;
import java.io.IOException;
import net.apkc.emma.tasks.Task;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.Server;
import org.apache.log4j.Logger;

/**
 * This is a task that launches the Cerebellum.
 *
 * @author K-Zen
 */
public class BrainStartTask extends Task
{

    private static final Logger LOG = Logger.getLogger(BrainStartTask.class.getName());
    private int executionType;
    private int heapSize;

    private BrainStartTask()
    {
    }

    public static BrainStartTask newBuild()
    {
        return new BrainStartTask();
    }

    public BrainStartTask setExecutionType(int executionType)
    {
        this.executionType = executionType;
        return this;
    }

    public BrainStartTask setHeapSize(int heapSize)
    {
        this.heapSize = heapSize;
        return this;
    }

    @Override
    protected Object doInBackground() throws Exception
    {
        Configuration conf = new AIMEConfiguration().create();

        try {
            // Configure
            Server cerebellum = RPC.getServer(Brain.getInstance(),
                                              "0.0.0.0",
                                              conf.getInt("cerebellum.port", 14999),
                                              conf.getInt("cerebellum.handlers", 10),
                                              true,
                                              conf);
            // Load & write all AIME metadata.
            Brain
                    .getInstance()
                    .execute(Handler
                            .makeXMLRequest(BrainXMLData
                                    .newBuild()
                                    .setJob(BrainXMLData.JOB_MERGE)
                                    .setClazz(MetadataGeneral.Data.class)
                                    .setFunction(MetadataGeneral.Data.EXECUTION_TYPE.getMethodName())
                                    .setParam(Parameter
                                            .newBuild()
                                            .setType(Integer.class)
                                            .setData(executionType))));
            Brain
                    .getInstance()
                    .execute(Handler
                            .makeXMLRequest(BrainXMLData
                                    .newBuild()
                                    .setJob(BrainXMLData.JOB_MERGE)
                                    .setClazz(MetadataGeneral.Data.class)
                                    .setFunction(MetadataGeneral.Data.HEAP_SIZE.getMethodName())
                                    .setParam(Parameter
                                            .newBuild()
                                            .setType(Integer.class)
                                            .setData(heapSize))));

            // Load & write all CerebellumData
            Brain
                    .getInstance()
                    .execute(Handler
                            .makeXMLRequest(BrainXMLData
                                    .newBuild()
                                    .setJob(BrainXMLData.JOB_MERGE)
                                    .setClazz(MetadataGeneral.Data.class)
                                    .setFunction(MetadataGeneral.Data.AIME_LOCALITY.getMethodName())
                                    .setParam(Parameter
                                            .newBuild()
                                            .setType(Boolean.class)
                                            .setData(true))));
            Brain
                    .getInstance()
                    .execute(Handler
                            .makeXMLRequest(BrainXMLData
                                    .newBuild()
                                    .setJob(BrainXMLData.JOB_MERGE)
                                    .setClazz(MetadataGeneral.Data.class)
                                    .setFunction(MetadataGeneral.Data.FILTERS.getMethodName())
                                    .setParam(Parameter
                                            .newBuild()
                                            .setType(URLFilter[].class)
                                            .setData(new URLFilters(conf).getFilters()))));

            // Mark event
            LogEventHandler.addNewEvent(
                    new LogEventHandler("Cerebellum started.", "The Cerebellum has been initialized and it's ready to be used."),
                    AIMEConstants.INFO_EVENT.getIntegerConstant());

            // Start the server
            cerebellum.start();
            cerebellum.join();
        }
        catch (IOException e) {
            LOG.fatal("Impossible to initialize the Cerebellum. Error: " + e.toString(), e);
        }
        catch (InterruptedException e) {
            LOG.fatal("The Cerebellum was interrupted! Error: " + e.toString(), e);
        }

        return null;
    }

    @Override
    public void reportProgress(int progress)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
