package io.aime.fetcher;

import io.aime.aimemisc.digest.SignatureFactory;
import io.aime.brain.Brain;
import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.MetadataGeneral;
import io.aime.brain.xml.Handler;
import io.aime.crawl.AIMEWritable;
import io.aime.crawl.CrawlDatum;
import io.aime.fetcher.StatsAgent.Tools;
import io.aime.metadata.DocMetadata;
import io.aime.net.URLFilter;
import io.aime.net.URLFilterException;
import io.aime.net.URLFilters;
import io.aime.net.URLNormalizers;
import io.aime.parse.Parse;
import io.aime.parse.ParseException;
import io.aime.parse.ParseImplementation;
import io.aime.parse.ParseResult;
import io.aime.parse.ParseStatus;
import io.aime.parse.ParseText;
import io.aime.parse.ParserRun;
import io.aime.protocol.Content;
import io.aime.protocol.Protocol;
import io.aime.protocol.ProtocolFactory;
import io.aime.protocol.ProtocolNotFound;
import io.aime.protocol.ProtocolOutput;
import io.aime.protocol.ProtocolStatus;
import io.aime.protocol.RobotRules;
import io.aime.util.AIMEConfiguration;
import io.aime.util.AIMEConstants;
import io.aime.util.AIMEJob;
import io.aime.util.GeneralUtilities;
import io.aime.util.ProcessKiller;
import io.aime.util.URLUtil;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.SwingWorker;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapRunnable;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

/**
 * A queue-based fetcher. This fetcher uses a well-known model of one producer
 * (a QueueFeeder) and many consumers (FetcherThread-s). QueueFeeder reads input
 * fetch lists and populates a set of FetchItemQueue-s, which hold FetchItem-s
 * that describe the items to be fetched.
 *
 * <p>
 * There are as many queues as there are unique hosts, but at any given time
 * the total number of fetch items in all queues is less than a fixed number
 * (currently set to a multiple of the number of threads).</p>
 *
 * <p>
 * As items are consumed from the queues, the QueueFeeder continues to add
 * new input items, so that their total count_sent stays fixed (FetcherThread-s
 * may also add new items to the queues e.g. as a results of redirection) -
 * until all input items are exhausted, at which point the number of items in
 * the queues begins to decrease. When this number reaches 0 fetcher will
 * finish.</p>
 *
 * <p>
 * This fetcher implementation handles per-host blocking itself, instead of
 * delegating this work to protocol-specific plug-ins. Each per-host queue
 * handles its own "politeness" settings, such as the maximum number of
 * concurrent requests and crawl delay between consecutive requests - and also a
 * list of requests in progress, and the time the last request was finished. As
 * FetcherThread-s ask for new items to be fetched, queues may return eligible
 * items or null if for "politeness" reasons this host's queue is not yet
 * ready.</p>
 *
 * <p>
 * If there are still un-fetched items in the queues, but none of the items
 * are ready, FetcherThread-s will spin-wait until either some items become
 * available, or a timeout is reached (at which point the Fetcher will abort,
 * assuming the task is hung).</p>
 *
 * @author Andrzej Bialecki
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class Fetcher extends Configured implements Tool, MapRunnable<Text, CrawlDatum, Text, AIMEWritable>
{

    protected static final Logger LOG = Logger.getLogger(Fetcher.class.getName());
    private static final String CONTENT_REDIR = "content";
    private static final String PROTOCOL_REDIR = "protocol";
    public static final int PERM_REFRESH_TIME = 5;
    private OutputCollector<Text, AIMEWritable> output;
    private Reporter reporter;
    private String segmentName;
    private AtomicInteger activeThreads = new AtomicInteger(0);
    private AtomicInteger spinWaiting = new AtomicInteger(0);
    private long start = System.currentTimeMillis(); // start time of fetcher run
    private AtomicLong lastRequestStart = new AtomicLong(start);
    private AtomicLong bytes = new AtomicLong(0); // total bytes fetched
    private AtomicInteger pages = new AtomicInteger(0); // total pages fetched
    private AtomicInteger errors = new AtomicInteger(0); // total pages errored
    private boolean storingContent;
    private boolean parsing;
    private FetchItemQueues fetchQueues;
    private QueueFeeder feeder;

    public static class InputFormat extends SequenceFileInputFormat<Text, CrawlDatum>
    {

        /**
         * Don't split inputs, to keep things polite.
         *
         * @param job
         * @param nSplits
         *
         * @return
         *
         * @throws IOException
         */
        @Override
        public InputSplit[] getSplits(JobConf job, int nSplits) throws IOException
        {
            FileStatus[] files = listStatus(job);
            FileSplit[] splits = new FileSplit[files.length];
            for (int i = 0; i < files.length; i++)
            {
                FileStatus cur = files[i];
                splits[i] = new FileSplit(cur.getPath(), 0, cur.getLen(), (String[]) null);
            }

            return splits;
        }
    }

    /**
     * This class described the item to be fetched.
     */
    private static class FetchItem
    {

        String queueID;
        Text url;
        URL u;
        CrawlDatum datum;

        public FetchItem(Text url, URL u, CrawlDatum datum, String queueID)
        {
            this.url = url;
            this.u = u;
            this.datum = datum;
            this.queueID = queueID;
        }

        /**
         * Create an item. Queue id will be created based on
         * <code>byIP</code> argument, either as a protocol + hostname pair, or
         * protocol + IP address pair.
         */
        public static FetchItem create(Text url, CrawlDatum datum, boolean byIP)
        {
            String queueID;
            URL u;
            try
            {
                u = new URL(url.toString());
            }
            catch (Exception e)
            {
                LOG.warn("Impossible to parse URL: " + url, e);
                return null;
            }

            String proto = u.getProtocol().toLowerCase();
            String host;
            if (byIP)
            {
                try
                {
                    InetAddress addr = InetAddress.getByName(u.getHost());
                    host = addr.getHostAddress();
                }
                catch (UnknownHostException e)
                {
                    // Unable to resolve it, so don't fall back to host name.
                    LOG.warn("Impossible to resolve URL: " + u.getHost() + ", skipping ...");
                    return null; // Saltar este Item.
                }
            }
            else
            {
                host = u.getHost();
                if (host == null)
                {
                    LOG.warn("Unknown host for URL: " + url + ", skipping ...");
                    return null; // Saltar este Item.
                }
                host = host.toLowerCase();
            }

            queueID = proto + "://" + host;

            return new FetchItem(url, u, datum, queueID);
        }

        public CrawlDatum getDatum()
        {
            return datum;
        }

        public String getQueueID()
        {
            return queueID;
        }

        public Text getUrl()
        {
            return url;
        }

        public URL getURL2()
        {
            return u;
        }
    }

    /**
     * This class handles FetchItems which come from the same host ID (be it a
     * proto/hostname or proto/IP pair). It also keeps track of requests in
     * progress and elapsed time between requests.
     */
    private static class FetchItemQueue
    {

        List<FetchItem> queue = Collections.synchronizedList(new LinkedList<FetchItem>());
        Set<FetchItem> inProgress = Collections.synchronizedSet(new HashSet<FetchItem>());
        AtomicLong nextFetchTime = new AtomicLong();
        AtomicInteger exceptionCounter = new AtomicInteger();
        long crawlDelay;
        long minCrawlDelay;
        int maxThreads;
        Configuration conf;

        public FetchItemQueue(Configuration conf, int maxThreads, long crawlDelay, long minCrawlDelay)
        {
            this.conf = conf;
            this.maxThreads = maxThreads;
            this.crawlDelay = crawlDelay;
            this.minCrawlDelay = minCrawlDelay;
            // ready to start
            setEndTime(System.currentTimeMillis() - crawlDelay);
        }

        public synchronized int emptyQueue()
        {
            int presize = queue.size();
            queue.clear();

            return presize;
        }

        public int getQueueSize()
        {
            return queue.size();
        }

        public int getInProgressSize()
        {
            return inProgress.size();
        }

        public int incrementExceptionCounter()
        {
            return exceptionCounter.incrementAndGet();
        }

        public void finishFetchItem(FetchItem it, boolean asap)
        {
            if (it != null)
            {
                inProgress.remove(it);
                setEndTime(System.currentTimeMillis(), asap);
            }
        }

        public void addFetchItem(FetchItem it)
        {
            if (it == null)
            {
                return;
            }

            queue.add(it);
        }

        public void addInProgressFetchItem(FetchItem it)
        {
            if (it == null)
            {
                return;
            }

            inProgress.add(it);
        }

        public FetchItem getFetchItem()
        {
            if (inProgress.size() >= maxThreads)
            {
                return null;
            }

            if (nextFetchTime.get() > System.currentTimeMillis())
            {
                return null;
            }

            FetchItem it = null;

            if (queue.isEmpty())
            {
                return null;
            }

            try
            {
                it = queue.remove(0);
                inProgress.add(it);
            }
            catch (Exception e)
            {
                LOG.error("Impossible to remove FetchItem from queue or impossible to add it to queue InProgress. Error: " + e.toString(), e);
            }

            return it;
        }

        public synchronized void dump()
        {
            if (LOG.isInfoEnabled())
            {
                LOG.info("Fetcher Dump.");
                LOG.info("  [maxThreads]: " + maxThreads);
                LOG.info("  [inProgress]: " + inProgress.size());
                LOG.info("  [crawlDelay]: " + crawlDelay);
                LOG.info("  [minCrawlDelay]: " + minCrawlDelay);
                LOG.info("  [nextFetchTime]: " + nextFetchTime.get());
                LOG.info("  [now]: " + System.currentTimeMillis());
            }

            for (int i = 0; i < queue.size(); i++)
            {
                FetchItem it = queue.get(i);

                if (LOG.isInfoEnabled())
                {
                    LOG.info(i + ": " + it.url);
                }
            }
        }

        private void setEndTime(long endTime)
        {
            setEndTime(endTime, false);
        }

        private void setEndTime(long endTime, boolean asap)
        {
            if (!asap)
            {
                nextFetchTime.set(endTime + (maxThreads > 1 ? minCrawlDelay : crawlDelay));
            }
            else
            {
                nextFetchTime.set(endTime);
            }
        }
    }

    /**
     * Convenience class - a collection of queues that keeps track of the total
     * number of items, and provides items eligible for fetching from any queue.
     */
    private static class FetchItemQueues
    {

        public static final String DEFAULT_ID = "default";
        Map<String, FetchItemQueue> queues = new HashMap<>();
        AtomicInteger totalSize = new AtomicInteger(0);
        int maxThreads;
        boolean byIP;
        long crawlDelay;
        long minCrawlDelay;
        long timelimit = -1;
        int maxExceptionsPerQueue = -1;
        Configuration conf;

        public FetchItemQueues(Configuration conf)
        {
            this.conf = conf;
            maxThreads = conf.getInt("fetcher.threads.per.host", 1);
            // backward-compatible default setting
            byIP = conf.getBoolean("fetcher.threads.per.host.by.ip", false);
            crawlDelay = (long) (conf.getFloat("fetcher.server.delay", 1.0f) * 1000);
            minCrawlDelay = (long) (conf.getFloat("fetcher.server.min.delay", 0.0f) * 1000);
            timelimit = conf.getLong("fetcher.timelimit.mins", -1);
            maxExceptionsPerQueue = conf.getInt("fetcher.max.exceptions.per.queue", -1);
        }

        public int getTotalSize()
        {
            return totalSize.get();
        }

        public int getQueueCount()
        {
            return queues.size();
        }

        public void addFetchItem(Text url, CrawlDatum datum)
        {
            FetchItem it = FetchItem.create(url, datum, byIP);
            if (it != null)
            {
                addFetchItem(it);
            }
        }

        public synchronized void addFetchItem(FetchItem it)
        {
            FetchItemQueue fiq = getFetchItemQueue(it.queueID);
            fiq.addFetchItem(it);
            totalSize.incrementAndGet();
        }

        public void finishFetchItem(FetchItem it)
        {
            finishFetchItem(it, false);
        }

        public void finishFetchItem(FetchItem it, boolean asap)
        {
            FetchItemQueue fiq = queues.get(it.queueID);
            if (fiq == null)
            {
                LOG.warn("Attempting to finalize an item from unknown queue: " + it);
                return;
            }
            fiq.finishFetchItem(it, asap);
        }

        public synchronized FetchItemQueue getFetchItemQueue(String id)
        {
            FetchItemQueue fiq = queues.get(id);
            if (fiq == null)
            {
                // initialize queue
                fiq = new FetchItemQueue(conf, maxThreads, crawlDelay, minCrawlDelay);
                queues.put(id, fiq);
            }

            return fiq;
        }

        public synchronized FetchItem getFetchItem()
        {
            Iterator<Map.Entry<String, FetchItemQueue>> it = queues.entrySet().iterator();
            while (it.hasNext())
            {
                FetchItemQueue fiq = it.next().getValue();
                // reap empty queues
                if (fiq.getQueueSize() == 0 && fiq.getInProgressSize() == 0)
                {
                    it.remove();
                    continue;
                }

                FetchItem fit = fiq.getFetchItem();
                if (fit != null)
                {
                    totalSize.decrementAndGet();
                    return fit;
                }
            }

            return null;
        }

        // called only once the feeder has stopped
        public synchronized int checkTimelimit()
        {
            int count = 0;

            if (System.currentTimeMillis() >= timelimit && timelimit != -1)
            {
                // emptying the queues
                for (String id : queues.keySet())
                {
                    FetchItemQueue fiq = queues.get(id);
                    if (fiq.getQueueSize() == 0)
                    {
                        continue;
                    }

                    if (LOG.isInfoEnabled())
                    {
                        LOG.info("Queue: " + id + ", time limit!");
                    }

                    int deleted = fiq.emptyQueue();

                    for (int i = 0; i < deleted; i++)
                    {
                        totalSize.decrementAndGet();
                    }

                    count += deleted;
                }

                // there might also be a case where totalsize !=0 but number of queues
                // == 0
                // in which case we simply force it to 0 to avoid blocking
                if (totalSize.get() != 0 && queues.isEmpty())
                {
                    totalSize.set(0);
                }
            }

            return count;
        }

        /**
         * Increment the exception counter of a queue in case of an exception
         * e.g. timeout; when higher than a given threshold simply empty the
         * queue.
         *
         * @param queueid
         *
         * @return Number of purged items.
         */
        public synchronized int checkExceptionThreshold(String queueid)
        {
            FetchItemQueue fiq = queues.get(queueid);

            if (fiq == null)
            {
                return 0;
            }

            if (fiq.getQueueSize() == 0)
            {
                return 0;
            }

            int excCount = fiq.incrementExceptionCounter();
            if (maxExceptionsPerQueue != -1 && excCount >= maxExceptionsPerQueue)
            {
                // too many exceptions for items in this queue - purge it
                int deleted = fiq.emptyQueue();

                if (LOG.isInfoEnabled())
                {
                    LOG.info("Queue: " + queueid + ". Removed [" + deleted + "] URLs from queue due to [" + excCount + "] ocurred exceptions.");
                }

                for (int i = 0; i < deleted; i++)
                {
                    totalSize.decrementAndGet();
                }

                return deleted;
            }

            return 0;
        }

        public synchronized void dump()
        {
            for (String id : queues.keySet())
            {
                FetchItemQueue fiq = queues.get(id);
                if (fiq.getQueueSize() == 0)
                {
                    continue;
                }

                if (LOG.isInfoEnabled())
                {
                    LOG.info("Queue: " + id);
                }

                fiq.dump();
            }
        }
    }

    /**
     * This class feeds the queues with input items, and re-fills them as items
     * are consumed by FetcherThread-s.
     */
    private static class QueueFeeder extends Thread
    {

        private RecordReader<Text, CrawlDatum> reader;
        private FetchItemQueues queues;
        private int size;
        private long timelimit = -1;

        public QueueFeeder(RecordReader<Text, CrawlDatum> reader, FetchItemQueues queues, int size)
        {
            this.reader = reader;
            this.queues = queues;
            this.size = size;
            setDaemon(true);
            setName("QueueFeeder");
        }

        public void setTimeLimit(long tl)
        {
            timelimit = tl;
        }

        @Override
        public void run()
        {
            boolean hasMore = true;
            int cnt = 0;
            int timelimitcount = 0;

            while (hasMore)
            {
                if (System.currentTimeMillis() >= timelimit && timelimit != -1)
                {
                    // enough .. lets' simply
                    // read all the entries from the input without processing them
                    try
                    {
                        Text url = new Text();
                        CrawlDatum datum = new CrawlDatum();
                        hasMore = reader.next(url, datum);
                        timelimitcount++;
                    }
                    catch (IOException e)
                    {
                        LOG.error("QueueFeeder error reading input, record: " + cnt, e);
                        return;
                    }

                    continue;
                }

                int feed = size - queues.getTotalSize();
                if (feed <= 0)
                {
                    // If the buffer has been filled up, then dump the fetched documents and start processing them.
                    break;
                }
                else
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Feeding [" + feed + "] URLs ...");
                    }

                    while (feed > 0 && hasMore)
                    {
                        try
                        {
                            Text url = new Text();
                            CrawlDatum datum = new CrawlDatum();
                            hasMore = reader.next(url, datum);
                            if (hasMore)
                            {
                                queues.addFetchItem(url, datum);
                                cnt++;
                                feed--;
                            }
                        }
                        catch (IOException e)
                        {
                            LOG.error("QueueFeeder error reading input, record: " + cnt, e);
                            return;
                        }
                    }
                }
            }

            if (LOG.isInfoEnabled())
            {
                LOG.info("QueueFeeder finalizing: Total [" + cnt + "] records + [hit by time] limit: " + timelimitcount);
            }
        }
    }

    /**
     * This class picks items from queues and fetches the pages.
     */
    private class FetcherThread extends Thread
    {

        private Configuration conf;
        private URLFilter[] urlFilters;
        private ParserRun parseUtil;
        private URLNormalizers normalizers;
        private ProtocolFactory protocolFactory;
        private long maxCrawlDelay;
        private boolean byIP;
        private int maxRedirect;
        private String reprUrl;
        private boolean redirecting;
        private int redirectCount;
        private boolean ignoreExternalLinks;

        public FetcherThread(Configuration conf)
        {
            setDaemon(true); // don't hang JVM on exit
            setName("FetcherThread"); // use an informative name
            this.conf = conf;
            urlFilters = (URLFilter[]) Brain.getClient(conf).execute(Handler.makeXMLRequest(BrainXMLData.newBuild().setJob(BrainXMLData.JOB_REQUEST).setClazz(MetadataGeneral.Data.class).setFunction(MetadataGeneral.Data.FILTERS.getMethodName()))).get();
            parseUtil = new ParserRun(conf);
            protocolFactory = new ProtocolFactory(conf);
            normalizers = new URLNormalizers(conf, URLNormalizers.SCOPE_FETCHER);
            maxCrawlDelay = conf.getInt("fetcher.max.crawl.delay", 30) * 1000;
            // backward-compatible default setting
            byIP = conf.getBoolean("fetcher.threads.per.host.by.ip", true);
            maxRedirect = conf.getInt("http.redirect.max", 3);
            ignoreExternalLinks = conf.getBoolean("db.ignore.external.links", false);
        }

        @Override
        public void run()
        {
            activeThreads.incrementAndGet(); // count_sent threads
            FetchItem fit = null;

            try
            {
                while (true)
                {
                    fit = fetchQueues.getFetchItem();
                    if (fit == null)
                    {
                        if (feeder.isAlive() || fetchQueues.getTotalSize() > 0)
                        {
                            if (LOG.isDebugEnabled())
                            {
                                LOG.debug(getName() + ": Waiting ...");
                            }

                            // spin-wait.
                            spinWaiting.incrementAndGet();

                            try
                            {
                                Thread.sleep(500);
                            }
                            catch (Exception e)
                            {
                            }

                            spinWaiting.decrementAndGet();
                            continue;
                        }
                        else
                        {
                            // all done, finish this thread
                            return;
                        }
                    }

                    lastRequestStart.set(System.currentTimeMillis());
                    Text reprUrlWritable = (Text) fit.datum.getMetadata().get(AIMEConstants.WRITABLE_REPR_URL_KEY.getTextConstant());

                    if (reprUrlWritable == null)
                    {
                        reprUrl = fit.url.toString();
                    }
                    else
                    {
                        reprUrl = reprUrlWritable.toString();
                    }

                    try
                    {
                        LOG.info("Fetching: " + GeneralUtilities.trimURL(fit.url.toString(), 120));

                        // fetch the page
                        redirecting = false;
                        redirectCount = 0;
                        do
                        {
                            if (LOG.isDebugEnabled())
                            {
                                LOG.debug("[redirectCount]: " + redirectCount);
                            }

                            redirecting = false;
                            Protocol protocol = protocolFactory.getProtocol(fit.url.toString());
                            RobotRules rules = protocol.getRobotRules(fit.url, fit.datum);

                            // ### Si AIME es denegado por el archivo robots.txt.
                            if (!rules.isAllowed(fit.u))
                            {
                                // Denegado por archivo "robots.txt".
                                fetchQueues.finishFetchItem(fit, true);

                                if (LOG.isDebugEnabled())
                                {
                                    LOG.debug("Denied by robots.txt: " + fit.url);
                                }

                                Tools.incrementStatusCodes(ProtocolStatus.ROBOTS_DENIED);
                                output(fit.url, fit.datum, null, ProtocolStatus.STATUS_ROBOTS_DENIED, CrawlDatum.STATUS_FETCH_GONE);

                                continue; // Saltar este ciclo del bucle.
                            }

                            if (rules.getCrawlDelay() > 0)
                            {
                                if (rules.getCrawlDelay() > maxCrawlDelay)
                                {
                                    // Unblock.
                                    fetchQueues.finishFetchItem(fit, true);

                                    if (LOG.isDebugEnabled())
                                    {
                                        LOG.debug("Crawl-Delay for: " + fit.url + " too high (" + rules.getCrawlDelay() + "), skipping ...");
                                    }

                                    Tools.incrementStatusCodes(ProtocolStatus.ROBOTS_DENIED);
                                    output(fit.url, fit.datum, null, ProtocolStatus.STATUS_ROBOTS_DENIED, CrawlDatum.STATUS_FETCH_GONE);

                                    continue; // Saltar este ciclo del bucle.
                                }
                                else
                                {
                                    FetchItemQueue fiq = fetchQueues.getFetchItemQueue(fit.queueID);
                                    fiq.crawlDelay = rules.getCrawlDelay();
                                }
                            }

                            ProtocolOutput output = protocol.getProtocolOutput(fit.url, fit.datum);
                            ProtocolStatus status = output.getStatus();
                            Content content = output.getContent();
                            ParseStatus pstatus;
                            // Unblock Queue.
                            fetchQueues.finishFetchItem(fit);
                            String urlString = fit.url.toString();

                            if (LOG.isDebugEnabled())
                            {
                                LOG.debug("Status Code for: " + urlString + " is: " + status.getCode());
                            }

                            switch (status.getCode())
                            {
                                case ProtocolStatus.WOULDBLOCK:
                                    Tools.incrementStatusCodes(status.getCode());
                                    // Retry...?
                                    fetchQueues.addFetchItem(fit);
                                    break;
                                case ProtocolStatus.SUCCESS: // Got a page.
                                    Tools.incrementStatusCodes(status.getCode());
                                    pstatus = output(fit.url, fit.datum, content, status, CrawlDatum.STATUS_FETCH_SUCCESS);
                                    updateStatus(content.getContent().length);

                                    if (pstatus != null && pstatus.isSuccess() && pstatus.getMinorCode() == ParseStatus.SUCCESS_REDIRECT)
                                    {
                                        String newUrl = pstatus.getMessage();
                                        int refreshTime = Integer.valueOf(pstatus.getArgs()[1]);
                                        Text redirUrl = handleRedirect(fit.url, fit.datum, urlString, newUrl, refreshTime < Fetcher.PERM_REFRESH_TIME, Fetcher.CONTENT_REDIR);

                                        if (redirUrl != null)
                                        {
                                            CrawlDatum newDatum = new CrawlDatum(CrawlDatum.STATUS_DB_UNFETCHED, fit.datum.getFetchInterval(), fit.datum.getScore());

                                            if (reprUrl != null)
                                            {
                                                newDatum.getMetadata().put(AIMEConstants.WRITABLE_REPR_URL_KEY.getTextConstant(), new Text(reprUrl));
                                            }

                                            fit = FetchItem.create(redirUrl, newDatum, byIP);

                                            if (fit != null)
                                            {
                                                FetchItemQueue fiq = fetchQueues.getFetchItemQueue(fit.queueID);
                                                fiq.addInProgressFetchItem(fit);
                                            }
                                            else
                                            {
                                                // stop redirecting
                                                redirecting = false;
                                            }
                                        }
                                    }
                                    break;
                                case ProtocolStatus.MOVED:
                                    Tools.incrementStatusCodes(status.getCode());
                                    fit = handleMoved(status, fit, content, urlString);
                                    break;
                                case ProtocolStatus.TEMP_MOVED:
                                    Tools.incrementStatusCodes(status.getCode());
                                    fit = handleMoved(status, fit, content, urlString);
                                    break;
                                case ProtocolStatus.EXCEPTION:
                                    // Generic exception.
                                    logError(fit.url, status.getMessage());
                                    int killedURLs = fetchQueues.checkExceptionThreshold(fit.getQueueID());
                                    if (killedURLs != 0 && LOG.isInfoEnabled())
                                    {
                                        LOG.info("The exceptions threshold for a queue has been reached. Emptying the queue ...");
                                    }
                                    output(fit.url, fit.datum, null, status, CrawlDatum.STATUS_FETCH_RETRY);
                                    break;
                                case ProtocolStatus.RETRY: // Retry.
                                    Tools.incrementStatusCodes(status.getCode());
                                    output(fit.url, fit.datum, null, status, CrawlDatum.STATUS_FETCH_RETRY);
                                    break;
                                case ProtocolStatus.BLOCKED:
                                    Tools.incrementStatusCodes(status.getCode());
                                    output(fit.url, fit.datum, null, status, CrawlDatum.STATUS_FETCH_RETRY);
                                    break;
                                case ProtocolStatus.GONE: // Gone.
                                    Tools.incrementStatusCodes(status.getCode());
                                    output(fit.url, fit.datum, null, status, CrawlDatum.STATUS_FETCH_GONE);
                                    break;
                                case ProtocolStatus.NOTFOUND:
                                    Tools.incrementStatusCodes(status.getCode());
                                    output(fit.url, fit.datum, null, status, CrawlDatum.STATUS_FETCH_GONE);
                                    break;
                                case ProtocolStatus.ACCESS_DENIED:
                                    Tools.incrementStatusCodes(status.getCode());
                                    output(fit.url, fit.datum, null, status, CrawlDatum.STATUS_FETCH_GONE);
                                    break;
                                case ProtocolStatus.ROBOTS_DENIED:
                                    Tools.incrementStatusCodes(status.getCode());
                                    output(fit.url, fit.datum, null, status, CrawlDatum.STATUS_FETCH_GONE);
                                    break;
                                case ProtocolStatus.NOTMODIFIED:
                                    Tools.incrementStatusCodes(status.getCode());
                                    output(fit.url, fit.datum, null, status, CrawlDatum.STATUS_FETCH_NOTMODIFIED);
                                    break;
                                default:
                                    Tools.incrementStatusCodes(status.getCode());
                                    output(fit.url, fit.datum, null, status, CrawlDatum.STATUS_FETCH_RETRY);
                                    LOG.warn("ProtocolStatus unknown: " + status.getCode());
                            }

                            if (redirecting && (redirectCount > maxRedirect))
                            {
                                fetchQueues.finishFetchItem(fit);

                                if (LOG.isInfoEnabled())
                                {
                                    LOG.info("Redirections exceeded: " + fit.url);
                                }

                                Tools.incrementStatusCodes(ProtocolStatus.REDIR_EXCEEDED);
                                output(fit.url, fit.datum, null, ProtocolStatus.STATUS_REDIR_EXCEEDED, CrawlDatum.STATUS_FETCH_GONE);
                            }
                        } while (redirecting && (redirectCount <= maxRedirect));
                    }
                    catch (ProtocolNotFound | NumberFormatException | MalformedURLException | URLFilterException t)
                    { // Unexpected exception.
                        // Unblock
                        fetchQueues.finishFetchItem(fit);
                        // Log as failed.
                        logError(fit.url, t.toString());
                        // Collect
                        output(fit.url, fit.datum, null, ProtocolStatus.STATUS_FAILED, CrawlDatum.STATUS_FETCH_RETRY);
                    }
                }
            }
            catch (Throwable t)
            {
                // Log as failed.
                logError(fit.url, t.toString());
            }
            finally
            {
                if (fit != null)
                {
                    fetchQueues.finishFetchItem(fit);
                }

                activeThreads.decrementAndGet(); // count_sent threads

                if (LOG.isInfoEnabled())
                {
                    LOG.info("Finishing thread: " + getName() + ". Active threads: " + activeThreads);
                }
            }
        }

        private Text handleRedirect(Text url, CrawlDatum datum, String urlString, String newUrl, boolean temp, String redirType) throws MalformedURLException, URLFilterException
        {
            newUrl = normalizers.normalize(newUrl, URLNormalizers.SCOPE_FETCHER);
            newUrl = URLFilters.filter(urlFilters, newUrl);

            if (ignoreExternalLinks)
            {
                try
                {
                    String origHost = new URL(urlString).getHost().toLowerCase();
                    String newHost = new URL(newUrl).getHost().toLowerCase();

                    if (!origHost.equals(newHost))
                    {
                        if (LOG.isDebugEnabled())
                        {
                            LOG.debug("Ignoring redirection [" + redirType + "] from [" + urlString + "] to [" + newUrl + "] due to ignored outlinks.");
                        }

                        return null;
                    }
                }
                catch (MalformedURLException e)
                {
                }
            }

            if (newUrl != null && !newUrl.equals(urlString))
            {
                reprUrl = URLUtil.chooseRepr(reprUrl, newUrl, temp);
                url = new Text(newUrl);

                if (maxRedirect > 0)
                {
                    redirecting = true;
                    redirectCount++;

                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("[" + redirType + "] redirects to: " + url + ". Searching now ...");
                    }

                    return url;
                }
                else
                {
                    CrawlDatum newDatum = new CrawlDatum(CrawlDatum.STATUS_LINKED, datum.getFetchInterval());

                    if (reprUrl != null)
                    {
                        newDatum.getMetadata().put(AIMEConstants.WRITABLE_REPR_URL_KEY.getTextConstant(), new Text(reprUrl));
                    }

                    output(url, newDatum, null, null, CrawlDatum.STATUS_LINKED);

                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("[" + redirType + "] redirects to: " + url + ". Searching later ...");
                    }

                    return null;
                }
            }
            else
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("[" + redirType + "] skipping redirection: " + (newUrl != null ? "Same URL" : "Filtering") + ".");
                }

                return null;
            }
        }

        /**
         * This method handles the URLs that have been moved.
         *
         * @param status    The protocol status object.
         * @param fit       The FetchItem object.
         * @param content   Contents of the document.
         * @param urlString The URL of the page.
         *
         * @throws MalformedURLException
         * @throws URLFilterException
         */
        private FetchItem handleMoved(ProtocolStatus status, FetchItem fit, Content content, String urlString) throws MalformedURLException, URLFilterException
        {
            int code;
            boolean temp;

            if (status.getCode() == ProtocolStatus.MOVED)
            {
                code = CrawlDatum.STATUS_FETCH_REDIR_PERM;
                temp = false;
            }
            else
            {
                code = CrawlDatum.STATUS_FETCH_REDIR_TEMP;
                temp = true;
            }

            output(fit.url, fit.datum, content, status, code);
            String newUrl = status.getMessage();
            Text redirUrl = handleRedirect(fit.url, fit.datum, urlString, newUrl, temp, Fetcher.PROTOCOL_REDIR);
            if (redirUrl != null)
            {
                CrawlDatum newDatum = new CrawlDatum(CrawlDatum.STATUS_DB_UNFETCHED, fit.datum.getFetchInterval(), fit.datum.getScore());

                if (reprUrl != null)
                {
                    newDatum.getMetadata().put(AIMEConstants.WRITABLE_REPR_URL_KEY.getTextConstant(), new Text(reprUrl));
                }

                fit = FetchItem.create(redirUrl, newDatum, byIP);

                if (fit != null)
                {
                    FetchItemQueue fiq = fetchQueues.getFetchItemQueue(fit.queueID);
                    fiq.addInProgressFetchItem(fit);
                }
                else
                {
                    // stop redirecting
                    redirecting = false;
                }
            }
            else
            {
                // stop redirecting
                redirecting = false;
            }

            return fit;
        }

        private void logError(Text url, String message)
        {
            LOG.warn("Fetching of: " + url + " failed. Cause: " + message);

            // Mark as failed.
            Tools.incrementStatusCodes(ProtocolStatus.FAILED);
            Tools.incrementStatusCodes(ProtocolStatus.EXCEPTION);

            errors.incrementAndGet(); // Increment counter.
        }

        private ParseStatus output(Text key, CrawlDatum datum, Content content, ProtocolStatus pstatus, int status)
        {
            datum.setStatus(status);
            datum.setFetchTime(System.currentTimeMillis());

            if (pstatus != null)
            {
                datum.getMetadata().put(AIMEConstants.WRITABLE_PROTO_STATUS_KEY.getTextConstant(), pstatus);
            }

            ParseResult parseResult = null;
            if (content != null)
            {
                DocMetadata metadata = content.getMetadata();
                // add segment to metadata
                metadata.set(AIMEConstants.SEGMENT_NAME_KEY.getStringConstant(), segmentName);
                // add score to content metadata so that ParseSegment can pick it up.
                try
                {
                    content.getMetadata().set(AIMEConstants.SCORE_KEY.getStringConstant(), "" + datum.getScore());
                }
                catch (Exception e)
                {
                    LOG.warn("Impossible to pass score. URL: " + key + " (" + e + ")", e);
                }

                // Note: Fetcher will only follow meta-redirects coming from the
                // original URL.
                if (parsing && status == CrawlDatum.STATUS_FETCH_SUCCESS)
                {
                    try
                    {
                        parseResult = parseUtil.getParseResult(content);
                    }
                    catch (ParseException e)
                    {
                        LOG.warn("Error parsing: " + key + ":" + StringUtils.stringifyException(e));
                    }

                    // If the document cannot be parsed, then compute the digest on the raw content.
                    if (parseResult == null)
                    {
                        long signature = SignatureFactory.getSignature(getConf()).calculate(content.toString());
                        datum.setSignature(signature);
                    }
                }

                // Store status code in content So we can read this value during
                // parsing (as a separate job) and decide to get_parse_result or not.
                content.getMetadata().add(AIMEConstants.FETCH_STATUS_KEY.getStringConstant(), Integer.toString(status));
            }

            try
            {
                output.collect(key, new AIMEWritable(datum));

                if (content != null && storingContent)
                {
                    output.collect(key, new AIMEWritable(content));
                }

                if (parseResult != null)
                {
                    for (Entry<Text, Parse> entry : parseResult)
                    {
                        Text url = entry.getKey();
                        Parse parse = entry.getValue();
                        ParseStatus parseStatus = parse.getData().getStatus();

                        if (!parseStatus.isSuccess())
                        {
                            LOG.warn("Error parsing: " + key + ":" + parseStatus);
                            parse = parseStatus.getEmptyParse(getConf());
                        }

                        // Calculate page signature. For non-parsing fetchers this will
                        // be done in ParseSegment
                        long signature = SignatureFactory.getSignature(getConf()).calculate(parse.getText());
                        // Ensure segment name and score are in parseData metadata
                        parse.getData().getContentMeta().set(AIMEConstants.SEGMENT_NAME_KEY.getStringConstant(), segmentName);
                        parse.getData().getContentMeta().set(AIMEConstants.SIGNATURE_KEY.getStringConstant(), String.valueOf(signature));
                        // Pass fetch time to content meta
                        parse.getData().getContentMeta().set(AIMEConstants.FETCH_TIME_KEY.getStringConstant(), Long.toString(datum.getFetchTime()));

                        if (url.equals(key))
                        {
                            datum.setSignature(signature);
                        }

                        try
                        {
                            parse.getData().getContentMeta().set(AIMEConstants.SCORE_KEY.getStringConstant(), content.getMetadata().get(AIMEConstants.SCORE_KEY.getStringConstant()));
                        }
                        catch (Exception e)
                        {
                            LOG.warn("Impossible to pass score. URL: " + key + " (" + e + ")", e);
                        }

                        // Collect the result.
                        output.collect(url, new AIMEWritable(new ParseImplementation(new ParseText(parse.getText()), parse.getData(), parse.isCanonical())));
                    }
                }
            }
            catch (IOException e)
            {
                LOG.error("Fetching generic exception: " + e.toString(), e);
            }

            // return get_parse_result status if it exits
            if (parseResult != null && !parseResult.isEmpty())
            {
                Parse p = parseResult.get(content.getUrl());

                if (p != null)
                {
                    return p.getData().getStatus();
                }
            }

            return null;
        }
    }

    public Fetcher()
    {
        super(null);
    }

    public Fetcher(Configuration conf)
    {
        super(conf);
    }

    /**
     * This method updates the status of the Fetcher.
     *
     * @param bytesInPage The amount of bytes in a given page.
     */
    private void updateStatus(int bytesInPage)
    {
        pages.incrementAndGet();
        bytes.addAndGet(bytesInPage);
    }

    /**
     * This method sends reports to Hadoop Reporter for the job, and also to the
     * FetcherStats.
     */
    private void reportStatus()
    {
        long elapsed = (System.currentTimeMillis() - start) / 1000;

        // Send status to reporter, so that it knows we are alive.
        StringBuilder status = new StringBuilder();
        status.append("ActiveThreads: ").append(activeThreads).append(", ");
        status.append("Pages: ").append(pages).append(", ");
        status.append("Errors: ").append(errors).append(", ");
        status.append("Speed: ").append(Math.round(((float) pages.get() * 10) / elapsed) / 10.0).append(" p/s # ").append(Math.round((((float) bytes.get() * 8) / 1024) / elapsed)).append(" Kbps");

        reporter.setStatus(status.toString());

        // Send status to FetcherStats, so that it can show in AIME's Dashboard.
        Tools.setFetcherStatus(activeThreads.get(), spinWaiting.get(), fetchQueues.getTotalSize(), pages.get(), errors.get(), elapsed, bytes.get());
    }

    @Override
    public void configure(JobConf job)
    {
        setConf(job);
        segmentName = job.get(AIMEConstants.SEGMENT_NAME_KEY.getStringConstant());
        storingContent = Fetcher.isStoringContent(job);
        parsing = Fetcher.isParsing(job);
    }

    public void close()
    {
    }

    public static boolean isParsing(Configuration conf)
    {
        return conf.getBoolean("fetcher.parse", true);
    }

    public static boolean isStoringContent(Configuration conf)
    {
        return conf.getBoolean("fetcher.store.content", true);
    }

    @Override
    public void run(RecordReader<Text, CrawlDatum> input, OutputCollector<Text, AIMEWritable> output, Reporter reporter) throws IOException
    {
        this.output = output;
        this.reporter = reporter;
        fetchQueues = new FetchItemQueues(getConf());
        int threadCount = getConf().getInt("fetcher.threads.fetch", 10);
        int buffer = Integer.MAX_VALUE;
        feeder = new QueueFeeder(input, fetchQueues, buffer);

        // the value of the time limit is either -1 or the time where it should finish
        long timelimit = getConf().getLong("fetcher.timelimit.mins", -1);
        if (timelimit != -1)
        {
            feeder.setTimeLimit(timelimit);
        }

        feeder.start();

        // set non-blocking & no-robots mode for HTTP protocol plugins.
        getConf().setBoolean(Protocol.CHECK_BLOCKING, false);
        getConf().setBoolean(Protocol.CHECK_ROBOTS, false);

        for (int i = 0; i < threadCount; i++)
        { // spawn threads
            new FetcherThread(getConf()).start();
        }

        // select a timeout that avoids a task timeout
        long timeout = getConf().getInt("mapred.task.timeout", 10 * 60 * 1000) / 2;
        do
        { // wait for threads to exit
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                // Do something ...
            }

            reportStatus();

            if (!feeder.isAlive() && fetchQueues.getTotalSize() < 5)
            {
                fetchQueues.dump();
            }

            // check timelimit
            if (!feeder.isAlive())
            {
                int hitByTimeLimit = fetchQueues.checkTimelimit();
                if (hitByTimeLimit != 0)
                {
                    reporter.incrCounter("FetcherStatus", "hitByTimeLimit", hitByTimeLimit);
                }
            }

            // some requests seem to hang, despite all intentions
            if ((System.currentTimeMillis() - lastRequestStart.get()) > timeout)
            {
                LOG.warn("Aborting with: " + activeThreads + " hung threads.");
                return;
            }

        } while (activeThreads.get() > 0);
    }

    public void fetch(Path segment, int threads, boolean parsing) throws IOException
    {
        checkConfiguration();

        if (LOG.isInfoEnabled())
        {
            LOG.info("Segment: " + segment);
        }

        // set the actual time for the timelimit relative
        // to the beginning of the whole job and not of a specific task
        // otherwise it keeps trying again if a task fails
        long timelimit = getConf().getLong("fetcher.timelimit.mins", -1);
        if (timelimit != -1)
        {
            timelimit = System.currentTimeMillis() + (timelimit * 60 * 1000);

            if (LOG.isInfoEnabled())
            {
                LOG.info("Time Limit: " + timelimit + " minutes");
            }

            getConf().setLong("fetcher.timelimit.mins", timelimit);
        }

        JobConf job = new AIMEJob(getConf());
        job.setJobName("Fetcher#Segment:[" + segment + "]");
        job.setInt("fetcher.threads.fetch", threads);
        job.set(AIMEConstants.SEGMENT_NAME_KEY.getStringConstant(), segment.getName());
        job.setBoolean("fetcher.parse", parsing);
        // for politeness, don't permit parallel execution of a single task
        job.setSpeculativeExecution(false);
        FileInputFormat.addInputPath(job, new Path(segment, CrawlDatum.GENERATE_DIR_NAME));
        job.setInputFormat(InputFormat.class);
        job.setMapRunnerClass(Fetcher.class);
        FileOutputFormat.setOutputPath(job, segment);
        job.setOutputFormat(FetcherOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(AIMEWritable.class);
        JobClient.runJob(job);
    }

    public static int runProcess(String[] args, SwingWorker<?, ?> runner) throws Exception
    {
        Configuration conf = new AIMEConfiguration().create();
        return ProcessKiller.checkExitCode(
                ToolRunner.run(conf, new Fetcher(), args),
                AIMEConstants.FETCHER_JOB_NAME.getStringConstant(),
                runner,
                conf);
    }

    @Override
    public int run(String[] args) throws Exception
    {
        StatsAgent agent = new StatsAgent();
        Path segment = new Path(args[0]);
        int threads = getConf().getInt("fetcher.threads.fetch", 10);
        boolean prg = true;

        for (int i = 1; i < args.length; i++)
        {
            switch (args[i])
            {
                case "-threads":
                    threads = Integer.parseInt(args[++i]);
                    break;
                case "-noParsing":
                    prg = false;
                    break;
            }
        }

        getConf().setInt("fetcher.threads.fetch", threads);

        if (!prg)
        {
            getConf().setBoolean("fetcher.parse", prg);
        }

        try
        {
            agent.init();
            fetch(segment, threads, prg);
            agent.stopAll();

            return 0;
        }
        catch (Exception e)
        {
            LOG.fatal("Error running Fetcher. Error: " + StringUtils.stringifyException(e));
            return -1;
        }
    }

    /**
     * This method checks to see if the robot is properly configured.
     */
    private void checkConfiguration()
    {
        // Ensure that a value has been set for the agent name.
        String agentName = getConf().get("http.agent.name");
        if (agentName == null || agentName.trim().length() == 0)
        {
            String message = "The robot's User Agent is empty. Please set it up at property [http.agent.name].";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }

        // Get all of the agents that we advertise.
        String agentNames = getConf().get("http.robots.agents");
        if (agentNames == null || agentNames.trim().length() == 0)
        {
            String message = "Our robot's User Agents are empty. Please set it up at property [http.robots.agents].";
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
    }
}
