package io.aime.protocol;

// Apache Hadoop
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.io.Text;

// AIME
import io.aime.crawl.CrawlDatum;
import io.aime.plugin.Pluggable;

/**
 * A retriever of url content. Implemented by protocol extensions.
 *
 * @author Nutch.org
 * @author K-Zen
 */
public interface Protocol extends Pluggable, Configurable {

    public final static String X_POINT_ID = Protocol.class.getName();
    /**
     * Property name. If in the current configuration this property is set to
     * true, protocol implementations should handle "politeness" limits
     * internally. If this is set to false, it is assumed that these limits are
     * enforced elsewhere, and protocol implementations should not enforce them
     * internally.
     */
    public final static String CHECK_BLOCKING = "protocol.plugin.check.blocking";
    /**
     * Property name. If in the current configuration this property is set to
     * true, protocol implementations should handle robot exclusion rules
     * internally. If this is set to false, it is assumed that these limits are
     * enforced elsewhere, and protocol implementations should not enforce them
     * internally.
     */
    public final static String CHECK_ROBOTS = "protocol.plugin.check.robots";

    /**
     * Returns the {@link Content} for a fetchlist entry.
     *
     * @param url
     * @param datum
     *
     * @return
     */
    ProtocolOutput getProtocolOutput(Text url, CrawlDatum datum);

    /**
     * Retrieve robot rules applicable for this url.
     *
     * @param url   url to check
     * @param datum page datum
     *
     * @return robot rules (specific for this url or default), never null
     */
    RobotRules getRobotRules(Text url, CrawlDatum datum);
}
