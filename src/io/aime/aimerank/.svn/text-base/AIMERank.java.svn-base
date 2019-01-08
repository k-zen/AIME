package io.aime.aimerank;

import org.apache.hadoop.conf.Configuration;

/**
 * This class handles all the scoring/ranking operations of AIME.
 *
 * @author Andreas P. Koenzen <akc@apkc.net>
 * @version 0.2
 */
public final class AIMERank
{

    /**
     * This method calculates the gravity value for a certain timestamp.
     *
     * @param timestamp The timestamp value.
     *
     * @return The gravity value for a given timestamp.
     */
    public static double calculateGravity(String timestamp)
    {
        // Info: Convert timestamp from milliseconds to seconds for weaker gravity.
        // Info: Also, raise the value of the constant for weaker gravity.
        float tau = Long.parseLong(timestamp);
        final float C = 512;

        return (1 / (Math.exp(-C * Normalizer.normalize(tau) + C)));
    }

    /**
     * This method calculates the gravity value for a certain timestamp,
     * avoiding the conversion to seconds. This gives a lot lower granularity,
     * and its perfect for using when high differences between times is not
     * necessary.
     *
     * @param timestamp The timestamp value.
     *
     * @return The gravity value for a given timestamp.
     */
    public static double calculateGravityLowGranularity(long timestamp)
    {
        // Info: Convert timestamp from milliseconds to seconds for weaker gravity.
        // Info: Also, raise the value of the constant for weaker gravity.
        float tau = timestamp;
        final float C = 512;

        return (1 / (Math.exp(-C * Normalizer.normalize(tau) + C)));
    }

    /**
     * This method calculates the fetch interval with gravity for a given score.
     * This new system gives re-fetching preferences to more newer pages,
     * instead of re-fetching everything.
     *
     * <p>
     * For calculating the score with gravity this method works best with the
     * given method calculateGravityLowGranularity().
     * </p>
     *
     * @param conf  The configuration object.
     * @param score The score with gravity of the page.
     *
     * @return The new fetching interval.
     */
    public static long calculateFetchInterval(Configuration conf, float score)
    {
        long tau = conf.getLong("db.fetch.interval.default", 86400);
        double omega = score;
        double delta = 10.00f;

        return Math.round(tau * (delta - omega) + tau);
    }
}
