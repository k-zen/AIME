package io.aime.crawl;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.io.Text;

/**
 * This interface defines the contract for implementations that manipulate fetch
 * times and re-fetch intervals.
 *
 * @author Andrzej Bialecki
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public interface FetchSchedule extends Configurable
{

    /**
     * It is unknown whether page was changed since our last visit.
     */
    public static final int STATUS_UNKNOWN = 0;
    /**
     * Page is known to have been modified since our last visit.
     */
    public static final int STATUS_MODIFIED = 1;
    /**
     * Page is known to remain unmodified since our last visit.
     */
    public static final int STATUS_NOTMODIFIED = 2;
    public static final int SECONDS_PER_DAY = 3600 * 24;

    /**
     * Initialize fetch schedule related data. Implementations should at least
     * set the <code>fetchTime</code> and <code>fetchInterval</code>.
     *
     * <p>
     * The default implementation set the <code>fetchTime</code> to now, using the default
     * <code>fetchInterval</code>.
     * </p>
     *
     * @param url   URL of the page.
     * @param datum datum instance to be initialized.
     *
     * @return Adjusted page information, including all original information.
     */
    public CrawlDatum initializeSchedule(Text url, CrawlDatum datum);

    /**
     * Sets the <code>fetchInterval</code> and <code>fetchTime</code> on a successfully fetched page.
     *
     * <p>
     * Implementations may use supplied arguments to support different
     * re-fetching schedules.
     * </p>
     *
     * @param url              url of the page
     * @param datum            page description to be adjusted. NOTE: this
     *                         instance, passed by reference, may be modified
     *                         inside the method.
     * @param prevFetchTime    previous value of fetch time, or 0 if not
     *                         available
     * @param prevModifiedTime previous value of modifiedTime, or 0 if not
     *                         available
     * @param fetchTime        the latest time, when the page was recently
     *                         re-fetched. Most FetchSchedule implementations
     *                         should update the value in {
     * @param modifiedTime     last time the content was modified. This
     *                         information comes from the protocol
     *                         implementations, or is set to &lt; 0 if not
     *                         available. Most FetchSchedule implementations
     *                         should update the value in {
     * @param state            if {@link #STATUS_MODIFIED}, then the content is
     *                         considered to be "changed" before the
     *                         <code>fetchTime</code>, if
     *                         {@link #STATUS_NOTMODIFIED} then the content is
     *                         known to be unchanged. This information may be
     *                         obtained by comparing page signatures before and
     *                         after fetching. If this is set to
     *                         {@link #STATUS_UNKNOWN}, then it is unknown
     *                         whether the page was changed; implementations are
     *                         free to follow a sensible default behavior.
     *
     * @return Adjusted page information, including all original information.
     */
    public CrawlDatum setFetchSchedule(Text url, CrawlDatum datum, long prevFetchTime, long prevModifiedTime, long fetchTime, long modifiedTime, int state);

    /**
     * This method specifies how to schedule refetching of pages marked as GONE.
     *
     * <p>
     * Default implementation increases fetchInterval by 50%, and if it
     * exceeds the <code>maxInterval</code> it calls
     * {@link #forceRefetch(Text, CrawlDatum, boolean)}.
     * </p>
     *
     * @param url              URL of the page
     * @param datum            datum instance to be adjusted
     * @param prevFetchTime
     * @param prevModifiedTime
     * @param fetchTime
     *
     * @return Adjusted page information, including all original information.
     */
    public CrawlDatum setPageGoneSchedule(Text url, CrawlDatum datum, long prevFetchTime, long prevModifiedTime, long fetchTime);

    /**
     * This method adjusts the fetch schedule if fetching needs to be re-tried
     * due to transient errors.
     *
     * <p>
     * The default implementation sets the next fetch time 1 day in the
     * future and increases the retry counter.
     * </p>
     *
     * @param url              URL of the page
     * @param datum            page information
     * @param prevFetchTime    previous fetch time
     * @param prevModifiedTime previous modified time
     * @param fetchTime        current fetch time
     *
     * @return Adjusted page information, including all original information.
     */
    public CrawlDatum setPageRetrySchedule(Text url, CrawlDatum datum, long prevFetchTime, long prevModifiedTime, long fetchTime);

    /**
     * Calculates last fetch time of the given CrawlDatum.
     *
     * @param datum
     *
     * @return the date as a long.
     */
    public long calculateLastFetchTime(CrawlDatum datum);

    /**
     * This method provides information whether the page is suitable for
     * selection in the current fetch list.
     *
     * <p>
     * NOTE: a true return value does not guarantee that the page will be
     * fetched, it just allows it to be included in the further selection
     * process based on scores. The default implementation checks
     * <code>fetchTime</code>, if it is higher than the
     * <code>curTime</code> it returns false, and true otherwise. It will also
     * check that fetchTime is not too remote (more * * than
     * <code>maxInterval</code>), in which case it lowers the interval and
     * returns true.
     * </p>
     *
     * @param url     URL of the page
     * @param datum   datum instance
     * @param curTime reference time (usually set to the time when the fetch
     *                list generation process was started).
     *
     * @return TRUE, if the page should be considered for inclusion in the
     *         current fetch list, otherwise false.
     */
    public boolean shouldFetch(Text url, CrawlDatum datum, long curTime);

    /**
     * This method resets fetchTime, fetchInterval, modifiedTime and page
     * signature, so that it forces re-fetching.
     *
     * @param url   URL of the page
     * @param datum datum instance
     * @param asap  if true, force refetch as soon as possible - this sets the
     *              fetchTime to now. If false, force refetch whenever the next
     *              fetch time is set.
     *
     * @return Adjusted page information, including all original information.
     */
    public CrawlDatum forceRefetch(Text url, CrawlDatum datum, boolean asap);
}
