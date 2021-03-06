package io.aime.crawl;

import io.aime.aimemisc.digest.SignatureComparator;
import io.aime.aimerank.AIMERank;
import io.aime.aimerank.Normalizer;
import io.aime.util.AIMEConfiguration;
import io.aime.util.AIMEConstants;
import io.aime.util.GeneralUtilities;
import io.aime.util.SeedTools;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.PriorityQueue;
import org.apache.log4j.Logger;

/**
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class CrawlDBReducer implements Reducer<Text, CrawlDatum, Text, CrawlDatum>
{

    private static final Logger LOG = Logger.getLogger(CrawlDBReducer.class.getName());
    private int retryMax;
    private CrawlDatum result = new CrawlDatum();
    private InlinkPriorityQueue linked = null;
    private boolean additionsAllowed;
    private int maxInterval;
    private FetchSchedule schedule;
    private float newlyDiscoveredPagesScore = 0.0f;
    private String[] seeds = SeedTools.getURLs();
    private int seedRefreshRate = 0;
    private Configuration conf;

    @Override
    public void configure(JobConf job)
    {
        conf = job;
        retryMax = job.getInt("db.fetch.retry.max", 3);
        additionsAllowed = job.getBoolean(CrawlDB.CRAWLDB_ADDITIONS_ALLOWED, true);
        int oldMaxInterval = job.getInt("db.max.fetch.interval", 0);
        maxInterval = (oldMaxInterval > 0 && job.getInt("db.fetch.interval.max", 0) == 0) ? oldMaxInterval * FetchSchedule.SECONDS_PER_DAY : job.getInt("db.fetch.interval.max", 0);
        schedule = FetchScheduleFactory.getFetchSchedule(job);
        int maxLinks = job.getInt("db.update.max.inlinks", 10000);
        linked = new InlinkPriorityQueue(maxLinks);
        newlyDiscoveredPagesScore = job.getFloat("db.score.newlydiscoveredlinks", 0.0f);
        seedRefreshRate = job.getInt("seed.refresh.rate", 900);
    }

    @Override
    public void close()
    {
    }

    @Override
    public void reduce(Text key, Iterator<CrawlDatum> values, OutputCollector<Text, CrawlDatum> output, Reporter reporter) throws IOException
    {
        CrawlDatum newEntry = new CrawlDatum();
        CrawlDatum oldEntry = new CrawlDatum();
        boolean fetchSet = false;
        boolean oldSet = false;
        long signature = 0L;
        boolean multiple = false; // avoid deep copy when only single value exists
        linked.clear();
        org.apache.hadoop.io.MapWritable metaFromParse = null;

        while (values.hasNext())
        {
            CrawlDatum datum = values.next();

            if (!multiple && values.hasNext())
            {
                multiple = true;
            }

            if (CrawlDatum.hasDBStatus(datum))
            {
                if (!oldSet)
                {
                    if (multiple)
                    {
                        oldEntry.set(datum);
                    }
                    else
                    {
                        oldEntry = datum; // no need for a deep copy - this is the only value
                    }

                    oldSet = true;
                }
                else
                {
                    if (oldEntry.getFetchTime() < datum.getFetchTime())
                    { // always take the latest version
                        oldEntry.set(datum);
                    }
                }

                continue;
            }

            if (CrawlDatum.hasFetchStatus(datum))
            {
                if (!fetchSet)
                {
                    if (multiple)
                    {
                        newEntry.set(datum);
                    }
                    else
                    {
                        newEntry = datum;
                    }

                    fetchSet = true;
                }
                else
                {
                    if (newEntry.getFetchTime() < datum.getFetchTime())
                    { // always take the latest version
                        newEntry.set(datum);
                    }
                }

                continue;
            }

            switch (datum.getStatus())
            { // collect other info
                case CrawlDatum.STATUS_LINKED:
                    CrawlDatum link;

                    if (multiple)
                    {
                        link = new CrawlDatum();
                        link.set(datum);
                    }
                    else
                    {
                        link = datum;
                    }

                    linked.insert(link);
                    break;
                case CrawlDatum.STATUS_SIGNATURE:
                    signature = datum.getSignature();
                    break;
                case CrawlDatum.STATUS_PARSE_META:
                    metaFromParse = datum.getMetadata();
                    break;
                default:
                    LOG.warn("Unknown state. Key: " + key + ", Datum: " + datum);
            }
        }

        // Copy the content of the queue into a List in reversed order.
        int numLinks = linked.size();
        List<CrawlDatum> linkList = new ArrayList<>(numLinks);
        for (int i = numLinks - 1; i >= 0; i--)
        {
            linkList.add(linked.pop());
        }

        // If it doesn't already exist, skip it.
        if (!oldSet && !additionsAllowed)
        {
            return;
        }

        // If there is no fetched datum, perhaps there is a link.
        if (!fetchSet && linkList.size() > 0)
        {
            newEntry = linkList.get(0);
            fetchSet = true;
        }

        // Still no new data - record only unchanged oldEntry data, if exists, and return.
        if (!fetchSet)
        {
            if (oldSet)
            { // at this point at least "oldEntry" should be present
                output.collect(key, oldEntry);
            }
            else
            {
                LOG.warn("Values of [fetch] and [old] missing. Signature: " + signature);
            }

            return;
        }

        if (signature == 0L)
        {
            signature = newEntry.getSignature();
        }

        long prevModifiedTime = oldSet ? oldEntry.getModifiedTime() : 0L;
        long prevFetchTime = oldSet ? oldEntry.getFetchTime() : 0L;

        // Initialize with the latest version, be it newEntry or link.
        result.set(newEntry);
        if (oldSet)
        {
            if (oldEntry.getMetadata().size() > 0)
            { // Copy metadata from oldEntry, if exists.
                result.putAllMetadata(oldEntry);

                if (newEntry.getMetadata().size() > 0)
                { // overlay with new, if any
                    result.putAllMetadata(newEntry);
                }
            }

            if (oldEntry.getModifiedTime() > 0 && newEntry.getModifiedTime() == 0)
            { // set the most recent valid value of modifiedTime
                result.setModifiedTime(oldEntry.getModifiedTime());
            }
        }

        switch (newEntry.getStatus())
        { // determine new status
            case CrawlDatum.STATUS_LINKED: // it was link
                if (oldSet)
                { // if oldEntry exists
                    result.set(oldEntry); // use it
                }
                else
                {
                    result = schedule.initializeSchedule(key, result);
                    result.setStatus(CrawlDatum.STATUS_DB_UNFETCHED);
                    result.setScore(newlyDiscoveredPagesScore);
                }

                break;
            case CrawlDatum.STATUS_FETCH_SUCCESS: // succesful newEntry
            case CrawlDatum.STATUS_FETCH_REDIR_TEMP: // successful newEntry, redirected
            case CrawlDatum.STATUS_FETCH_REDIR_PERM:
            case CrawlDatum.STATUS_FETCH_NOTMODIFIED: // successful newEntry, notmodified
                int modified = FetchSchedule.STATUS_UNKNOWN; // determine the modification status
                if (newEntry.getStatus() == CrawlDatum.STATUS_FETCH_NOTMODIFIED)
                {
                    modified = FetchSchedule.STATUS_NOTMODIFIED;
                }
                else
                {
                    if (oldSet && oldEntry.getSignature() != 0L && signature != 0L)
                    {
                        if (SignatureComparator._compare(oldEntry.getSignature(), signature) != 0)
                        {
                            modified = FetchSchedule.STATUS_MODIFIED;
                        }
                        else
                        {
                            modified = FetchSchedule.STATUS_NOTMODIFIED;
                        }
                    }
                }

                // Set the schedule.
                result = schedule.setFetchSchedule(key, result, prevFetchTime, prevModifiedTime, newEntry.getFetchTime(), newEntry.getModifiedTime(), modified);

                // Set the result status and signature.
                if (modified == FetchSchedule.STATUS_NOTMODIFIED)
                {
                    result.setStatus(CrawlDatum.STATUS_DB_NOTMODIFIED);
                    if (oldSet)
                    {
                        result.setSignature(oldEntry.getSignature());
                    }
                }
                else
                {
                    switch (newEntry.getStatus())
                    {
                        case CrawlDatum.STATUS_FETCH_SUCCESS:
                            result.setStatus(CrawlDatum.STATUS_DB_FETCHED);

                            // Set the discovery time to the time the page was first crawled. Never modify this value afterwards.
                            if (result.getDiscoveryTime() == 0L)
                            {
                                result.setDiscoveryTime(newEntry.getFetchTime());
                            }

                            // Calculate the score of the document with gravity, and also re-calculate the newEntry interval,
                            // according to this new value with gravity.
                            result.setScoreGravity(
                                    (float) Normalizer.normalize(
                                            result.getScore() / AIMERank.calculateGravityLowGranularity(System.currentTimeMillis() - result.getDiscoveryTime())) * 10);

                            if (LOG.isDebugEnabled())
                            {
                                LOG.debug(
                                        "URL:" + key + " "
                                        + "Score:" + result.getScore() + " "
                                        + "Now:" + System.currentTimeMillis() + "[" + new Date(System.currentTimeMillis()) + "]" + " "
                                        + "Modified Time:" + result.getDiscoveryTime() + "[" + new Date(result.getDiscoveryTime()) + "]");
                            }

                            // Set the fetch interval according to gravity.
                            result.setFetchInterval(AIMERank.calculateFetchInterval((conf != null) ? conf : new AIMEConfiguration().create(), result.getScoreGravity()));

                            // Refresh seeds every 15 minutes.
                            // URLs are added a trailing slash, remove it before comparing.
                            if (ArrayUtils.contains(seeds, StringUtils.chomp(key.toString(), "/")))
                            {
                                result.setFetchInterval(seedRefreshRate);

                                if (LOG.isInfoEnabled())
                                {
                                    LOG.info("Re-crawling: " + GeneralUtilities.trimURL(key.toString(), 80) + " in " + seedRefreshRate + " seconds.");
                                }
                            }

                            // Refresh every other page/document according to the gravity generated value.
                            result.setFetchTime(result.getFetchTime() + (long) result.getFetchInterval() * 1000);

                            if (LOG.isDebugEnabled())
                            {
                                LOG.debug(
                                        "URL:" + key + " "
                                        + "Score Gravity:" + result.getScoreGravity() + " "
                                        + "Interval:" + result.getFetchInterval());
                            }

                            break;
                        case CrawlDatum.STATUS_FETCH_REDIR_PERM:
                            result.setStatus(CrawlDatum.STATUS_DB_REDIR_PERM);
                            break;
                        case CrawlDatum.STATUS_FETCH_REDIR_TEMP:
                            result.setStatus(CrawlDatum.STATUS_DB_REDIR_TEMP);
                            break;
                        default:
                            LOG.warn("Unexpected state: " + newEntry.getStatus() + ". Setting to previous state.");

                            if (oldSet)
                            {
                                result.setStatus(oldEntry.getStatus());
                            }
                            else
                            {
                                result.setStatus(CrawlDatum.STATUS_DB_UNFETCHED);
                            }
                    }

                    result.setSignature(signature);

                    if (metaFromParse != null)
                    {
                        for (Entry<Writable, Writable> e : metaFromParse.entrySet())
                        {
                            result.getMetadata().put(e.getKey(), e.getValue());
                        }
                    }
                }

                // If fetchInterval is larger than the system-wide maximum, trigger
                // an unconditional recrawl. This prevents the page to be stuck at
                // NOTMODIFIED state, when the oldEntry fetched copy was already removed with
                // oldEntry segments.
                if (maxInterval < result.getFetchInterval())
                {
                    result = schedule.forceRefetch(key, result, false);
                }

                break;
            case CrawlDatum.STATUS_SIGNATURE:
                LOG.warn("Lonely [CrawlDatum.STATUS_SIGNATURE]: " + key);
                return;
            case CrawlDatum.STATUS_FETCH_RETRY: // temporary failure
                if (oldSet)
                {
                    result.setSignature(oldEntry.getSignature()); // use oldEntry signature
                }

                result = schedule.setPageRetrySchedule(key, result, prevFetchTime, prevModifiedTime, newEntry.getFetchTime());

                if (result.getRetriesSinceFetch() < retryMax)
                {
                    result.setStatus(CrawlDatum.STATUS_DB_UNFETCHED);
                }
                else
                {
                    result.setStatus(CrawlDatum.STATUS_DB_GONE);
                }

                break;
            case CrawlDatum.STATUS_FETCH_GONE: // permanent failure
                if (oldSet)
                {
                    result.setSignature(oldEntry.getSignature()); // use oldEntry signature
                }

                result.setStatus(CrawlDatum.STATUS_DB_GONE);
                result = schedule.setPageGoneSchedule(key, result, prevFetchTime, prevModifiedTime, newEntry.getFetchTime());
                break;
            default:
                throw new RuntimeException("Unknown state: " + newEntry.getStatus() + ", Key: " + key);
        }

        // Remove generation time, if any.
        result.getMetadata().remove(AIMEConstants.WRITABLE_GENERATE_TIME_KEY.getTextConstant());

        // Collect the result.
        output.collect(key, result);
    }
}

class InlinkPriorityQueue extends PriorityQueue<CrawlDatum>
{

    public InlinkPriorityQueue(int maxSize)
    {
        initialize(maxSize);
    }

    @Override
    protected boolean lessThan(Object arg0, Object arg1)
    {
        CrawlDatum candidate = (CrawlDatum) arg0;
        CrawlDatum least = (CrawlDatum) arg1;

        return candidate.getScore() > least.getScore();
    }
}
