package io.aime.plugins.protocolhttpclient;

// AIME
import io.aime.metadata.DocMetadata;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configurable;

// Log4j
import org.apache.log4j.Logger;

// Util
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides the Http protocol implementation with the ability to authenticate
 * when prompted.
 *
 * <p>
 * The goal is to provide multiple authentication types but for now just the
 * {@link HttpBasicAuthentication} authentication type is provided.
 * </p>
 *
 * @see HttpBasicAuthentication
 * @see Http
 * @see HttpResponse
 *
 * @author Matt Tencati
 * @author K-Zen
 */
public class HTTPAuthenticationFactory implements Configurable {

    /**
     * The HTTP Authentication (WWW-Authenticate) header which is returned by a
     * webserver requiring authentication.
     */
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String KEY = HTTPAuthenticationFactory.class.getName();
    private static final Logger LOG = Logger.getLogger(KEY);
    private Configuration conf = null;

    public HTTPAuthenticationFactory(Configuration conf) {
        this.setConf(conf);
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    @Override
    public Configuration getConf() {
        return conf;
    }

    public HTTPAuthentication findAuthentication(DocMetadata header) {
        if (header == null) {
            return null;
        }

        try {
            List<String> challenge = new ArrayList<String>();
            challenge.add(header.get(WWW_AUTHENTICATE));

            if (challenge == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Authentication challenge is null.");
                }

                return null;
            }

            Iterator<String> i = challenge.iterator();
            HTTPAuthentication auth = null;

            while (i.hasNext() && auth == null) {
                String challengeString = i.next();
                if (challengeString.equals("NTLM")) {
                    challengeString = "Basic realm=techweb";
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Checking challenge string: " + challengeString);
                }

                auth = HTTPBasicAuthentication.getAuthentication(challengeString, conf);
                if (auth != null) {
                    return auth;
                }
            }
        }
        catch (Exception e) {
            LOG.fatal("Impossible to find authentication for: " + header.toString(), e);
        }

        return null;
    }
}
