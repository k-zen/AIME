package io.aime.crawl;

import org.apache.hadoop.io.Text;

/**
 * This class implements the default re-fetch schedule.
 *
 * <p>
 * That is, no matter if the page was changed or not, the
 * <code>fetchInterval</code> remains unchanged, and the updated page fetchTime
 * will always be set to <code>fetchTime + fetchInterval * 1000</code>.
 * </p>
 *
 * @author Andrzej Bialecki
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class DefaultFetchSchedule extends AbstractFetchSchedule
{

    @Override
    public CrawlDatum setFetchSchedule(Text url, CrawlDatum datum, long prevFetchTime, long prevModifiedTime, long fetchTime, long modifiedTime, int state)
    {
        datum = super.setFetchSchedule(url, datum, prevFetchTime, prevModifiedTime, fetchTime, modifiedTime, state);
        if (datum.getFetchInterval() == 0)
        {
            datum.setFetchInterval(defaultInterval);
        }

        // Set here the default interval for all document that have not been
        // fetched yet. This is because fetched documents are configured in
        // CrawlDbReducer.
        if (datum.getStatus() != CrawlDatum.STATUS_FETCH_SUCCESS)
        {
            datum.setFetchTime(fetchTime + (long) datum.getFetchInterval() * 1000);
        }

        datum.setModifiedTime(modifiedTime);

        return datum;
    }
}
