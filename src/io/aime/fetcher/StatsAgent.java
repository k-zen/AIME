package io.aime.fetcher;

import io.aime.brain.Brain;
import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.MetadataFetcher;
import io.aime.brain.xml.Handler;
import io.aime.protocol.ProtocolStatus;
import io.aime.util.AIMEConfiguration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.hadoop.conf.Configuration;

/**
 * Agent to handle the Fetcher stats, then this information will be sent to the
 * Cerebellum.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
class StatsAgent
{

    // Status Table
    private static AtomicInteger suc = new AtomicInteger(0); // Success
    private static AtomicInteger fai = new AtomicInteger(0); // Failed
    private static AtomicInteger gon = new AtomicInteger(0); // Gone
    private static AtomicInteger mov = new AtomicInteger(0); // Moved
    private static AtomicInteger tem = new AtomicInteger(0); // Temp Moved
    private static AtomicInteger not = new AtomicInteger(0); // Not Found
    private static AtomicInteger ret = new AtomicInteger(0); // Retry
    private static AtomicInteger exc = new AtomicInteger(0); // Exception
    private static AtomicInteger acc = new AtomicInteger(0); // Access Denied
    private static AtomicInteger rob = new AtomicInteger(0); // Robots Denied
    private static AtomicInteger red = new AtomicInteger(0); // Redir Exceeded
    private static AtomicInteger mod = new AtomicInteger(0); // Not Modified
    private static AtomicInteger wou = new AtomicInteger(0); // Would Block
    private static AtomicInteger blo = new AtomicInteger(0); // Blocked
    private static AtomicInteger unk = new AtomicInteger(0); // Unknown
    // Stats Table
    private static AtomicLong act = new AtomicLong(0); // Active Threads
    private static AtomicLong wai = new AtomicLong(0); // Waiting Threads
    private static AtomicLong que = new AtomicLong(0); // Queue Size
    private static AtomicLong pag = new AtomicLong(0); // Pages
    private static AtomicLong err = new AtomicLong(0); // Errors
    private static AtomicLong ela = new AtomicLong(0); // Elapsed
    private static AtomicLong byt = new AtomicLong(0); // Bytes
    // Misc
    private Configuration conf = new AIMEConfiguration().create();
    private ThreadGroup tg = new ThreadGroup("FetcherStatsAgent");
    private Thread sender;

    void init()
    {
        this.sender = new Thread(this.tg, new Sender(), "FetcherStatsAgentSender");

        // Start the group.
        this.sender.start();

        if (Fetcher.LOG.isInfoEnabled()) {
            Fetcher.LOG.info("Fetcher's stats agent started!");
        }
    }

    void stopAll()
    {
        try {
            // Wait 1 minute before stoping the agent. So the latest stats can be synchronized.
            Thread.sleep(conf.getInt("fetcher.stats.agent.stoping.wait", 60000));

            this.tg.interrupt();

            if (Fetcher.LOG.isInfoEnabled()) {
                Fetcher.LOG.info("Fetcher's stats agent stopped!");
            }
        }
        catch (Exception e) {
            Fetcher.LOG.error("Error trying to stop FetcherStatsAgent's thread! Error: " + e.toString(), e);
        }
    }

    /**
     * Contains methods to gather stats.
     */
    static class Tools
    {

        static void incrementStatusCodes(int statusCode)
        {
            switch (statusCode) {
                case ProtocolStatus.SUCCESS:
                    suc.incrementAndGet();
                    break;
                case ProtocolStatus.FAILED:
                    fai.incrementAndGet();
                    break;
                case ProtocolStatus.GONE:
                    gon.incrementAndGet();
                    break;
                case ProtocolStatus.MOVED:
                    mov.incrementAndGet();
                    break;
                case ProtocolStatus.TEMP_MOVED:
                    tem.incrementAndGet();
                    break;
                case ProtocolStatus.NOTFOUND:
                    not.incrementAndGet();
                    break;
                case ProtocolStatus.RETRY:
                    ret.incrementAndGet();
                    break;
                case ProtocolStatus.EXCEPTION:
                    exc.incrementAndGet();
                    break;
                case ProtocolStatus.ACCESS_DENIED:
                    acc.incrementAndGet();
                    break;
                case ProtocolStatus.ROBOTS_DENIED:
                    rob.incrementAndGet();
                    break;
                case ProtocolStatus.REDIR_EXCEEDED:
                    red.incrementAndGet();
                    break;
                case ProtocolStatus.NOTMODIFIED:
                    mod.incrementAndGet();
                    break;
                case ProtocolStatus.WOULDBLOCK:
                    wou.incrementAndGet();
                    break;
                case ProtocolStatus.BLOCKED:
                    blo.incrementAndGet();
                    break;
                default:
                    unk.incrementAndGet();
                    break;
            }
        }

        /**
         * This method sets the values that correspond to the status of the
         * Fetcher.
         *
         * @param activeThreads  Number of active threads.
         * @param waitingThreads Number of waiting threads.
         * @param queueSize      The queue size.
         * @param pages          Number of pages fetched.
         * @param errors         Number of error produced.
         * @param elapsed        Elapsed time.
         * @param bytes          Bytes fetched.
         */
        static void setFetcherStatus(long activeThreads, long waitingThreads, long queueSize, long pages, long errors, long elapsed, long bytes)
        {
            act.set(activeThreads);
            wai.set(waitingThreads);
            que.set(queueSize);
            pag.set(pages);
            err.set(errors);
            ela.set(elapsed);
            byt.set(bytes);
        }
    }

    /**
     * Executes in the background, and sends the previously gatherer stats to
     * the Start.
     */
    class Sender implements Runnable
    {

        private final Configuration CONF = new AIMEConfiguration().create();

        @Override
        public void run()
        {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Brain
                            .getClient(CONF)
                            .execute(Handler
                                    .makeXMLRequest(
                                            BrainXMLData
                                            .newBuild()
                                            .setJob(BrainXMLData.JOB_MERGE)
                                            .setClazz(MetadataFetcher.class)
                                            .setFunction("Data")
                                            .setParam(BrainXMLData.Parameter
                                                    .newBuild()
                                                    .setType(MetadataFetcher.Data.class)
                                                    .setData(MetadataFetcher.Data
                                                            .newBuild()
                                                            .setSuccess(suc.get())
                                                            .setFailed(fai.get())
                                                            .setGone(gon.get())
                                                            .setMoved(mov.get())
                                                            .setTempMoved(tem.get())
                                                            .setNotFound(not.get())
                                                            .setRetry(ret.get())
                                                            .setException(exc.get())
                                                            .setAccessDenied(acc.get())
                                                            .setRobotsDenied(rob.get())
                                                            .setRedirExceeded(red.get())
                                                            .setNotModified(mod.get())
                                                            .setWouldBlock(wou.get())
                                                            .setBlocked(blo.get())
                                                            .setUnknown(unk.get())
                                                            .setActiveThreads(act.get())
                                                            .setWaitingThreads(wai.get())
                                                            .setQueueSize(que.get())
                                                            .setPages(pag.get())
                                                            .setErrors(err.get())
                                                            .setElapsed(ela.get())
                                                            .setBytes(byt.get())))));

                    if (Fetcher.LOG.isDebugEnabled()) {
                        Fetcher.LOG.debug("Sending stats to Cerebellum ...");
                    }

                    Thread.sleep(conf.getInt("fetcher.stats.agent.sync", 1000));
                }
                catch (InterruptedException e) {
                    // Do nothing
                }
            }
        }
    }
}
