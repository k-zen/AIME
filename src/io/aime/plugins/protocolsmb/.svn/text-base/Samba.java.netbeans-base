package io.aime.plugins.protocolsmb;

// AIME
import io.aime.crawl.CrawlDatum;
import io.aime.protocol.EmptyRobotRules;
import io.aime.protocol.Protocol;
import io.aime.protocol.ProtocolOutput;
import io.aime.protocol.ProtocolStatus;
import io.aime.protocol.RobotRules;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;

// Log4j
import org.apache.log4j.Logger;

// Net
import java.net.URL;

/**
 * This class deals with connections to Samba servers.
 *
 * @author K-Zen
 */
public class Samba implements Protocol {

    public static final Logger LOG = Logger.getLogger(Samba.class);
    static final int MAX_REDIRECTS = 5;
    int maxContentLength;
    private Configuration conf;

    /**
     * Set the point at which content is truncated.
     */
    public void setMaxContentLength(int length) {
        this.maxContentLength = length;
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
        this.maxContentLength = conf.getInt("smb.content.limit", 64 * 1024);
    }

    @Override
    public Configuration getConf() {
        return this.conf;
    }

    @Override
    public RobotRules getRobotRules(Text url, CrawlDatum datum) {
        return EmptyRobotRules.RULES;
    }

    @Override
    public ProtocolOutput getProtocolOutput(Text url, CrawlDatum datum) {
        String urlString = url.toString();

        try {
            URL u = new URL(urlString);
            int redirects = 0;

            while (true) {
                SambaResponse response;
                response = new SambaResponse(u, datum, this, getConf()); // make a request
                int code = response.getCode();

                if (code == 200) { // got a good response
                    return new ProtocolOutput(response.toContent()); // return it
                }
                else if (code >= 300 && code < 400) { // handle redirect
                    if (redirects == MAX_REDIRECTS) {
                        throw new SambaException("Too many redirects: " + url);
                    }

                    //u = new URL (response.getHeader ("Location"));
                    redirects++;

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Redirect to: " + u);
                    }
                }
                else { // convert to exception
                    throw new SambaError(code);
                }
            }
        }
        catch (Exception e) {
            LOG.fatal("Error getting samba file protocol output. Error: " + e.toString(), e);

            return new ProtocolOutput(null, new ProtocolStatus(e));
        }
    }
}
