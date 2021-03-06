package io.aime.protocol;

// Net
import java.net.URL;

/**
 * This class holds the rules which were parsed from a robots.txt file, and can
 * test paths against those rules.
 */
public interface RobotRules {

    /**
     * Get expire time
     * @return
     */
    public long getExpireTime();

    /**
     * Get Crawl-Delay, in milliseconds. This returns -1 if not set.
     * @return
     */
    public long getCrawlDelay();

    /**
     * Returns
     * <code>false</code> if the
     * <code>robots.txt</code> file prohibits us from accessing the given
     * <code>url</code>, or
     * <code>true</code> otherwise.
     * @param url
     * @return
     */
    public boolean isAllowed(URL url);
}
