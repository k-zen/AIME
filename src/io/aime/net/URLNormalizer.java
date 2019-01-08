package io.aime.net;

// Apache Hadoop
import org.apache.hadoop.conf.Configurable;

// Net
import java.net.MalformedURLException;

/**
 * Interface used to convert URLs to normal form and optionally perform
 * substitutions.
 *
 * @author K-Zen
 */
public interface URLNormalizer extends Configurable {

    /**
     * Extension ID
     */
    public static final String X_POINT_ID = URLNormalizer.class.getName();

    /**
     * Interface for URL normalization.
     * @param urlString
     * @param scope
     * @return
     * @throws MalformedURLException
     */
    public String normalize(String urlString, String scope) throws MalformedURLException;
}
