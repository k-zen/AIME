package io.aime.protocol;

// AIME
import io.aime.util.ObjectCache;

// Apache Hadoop
import io.aime.plugin.PluginRepository;
import io.aime.plugin.PluginRuntimeException;
import io.aime.plugin.Extension;
import io.aime.plugin.ExtensionPoint;
import org.apache.hadoop.conf.Configuration;

// Log4j
import org.apache.log4j.Logger;

// Net
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Creates and caches {@link Protocol} plugins. Protocol plugins should define
 * the attribute "protocolName" with the name of the protocol that they
 * implement.
 *
 * <p>
 * Configuration object is used for caching. Cache key is constructed from
 * appending protocol name (eg. http) to constant {@link Protocol#X_POINT_ID}.
 * </p>
 *
 * @author Nutch.org
 * @author K-Zen
 */
public class ProtocolFactory {

    private static final String KEY = ProtocolFactory.class.getName();
    private static final Logger LOG = Logger.getLogger(KEY);
    private ExtensionPoint extensionPoint;
    private Configuration conf;

    public ProtocolFactory(Configuration conf) {
        this.conf = conf;
        this.extensionPoint = PluginRepository.get(conf).getExtensionPoint(Protocol.X_POINT_ID);

        if (this.extensionPoint == null) {
            throw new RuntimeException("Punto X -> " + Protocol.X_POINT_ID + " no encontrado.");
        }
    }

    /**
     * Returns the appropriate {@link Protocol} implementation for a url.
     *
     * @param urlString Url String
     *
     * @return The appropriate {@link Protocol} implementation for a given
     *         {@link URL}.
     *
     * @throws ProtocolNotFound when Protocol can not be found for urlString
     */
    public Protocol getProtocol(String urlString) throws ProtocolNotFound {
        ObjectCache objectCache = ObjectCache.get(conf);

        try {
            URL url = new URL(urlString);
            String protocolName = url.getProtocol();
            String cacheId = Protocol.X_POINT_ID + protocolName;

            if (protocolName == null) {
                throw new ProtocolNotFound(urlString);
            }

            if (objectCache.getObject(cacheId) != null) {
                return (Protocol) objectCache.getObject(cacheId);
            }
            else {
                Extension extension = findExtension(protocolName);
                if (extension == null) {
                    throw new ProtocolNotFound(protocolName);
                }

                Protocol protocol = (Protocol) extension.getExtensionInstance();
                objectCache.setObject(cacheId, protocol);

                return protocol;
            }

        }
        catch (MalformedURLException e) {
            throw new ProtocolNotFound(urlString, e.toString());
        }
        catch (PluginRuntimeException e) {
            throw new ProtocolNotFound(urlString, e.toString());
        }
    }

    private Extension findExtension(String name) throws PluginRuntimeException {
        Extension[] extensions = this.extensionPoint.getExtensions();

        for (int i = 0; i < extensions.length; i++) {
            Extension extension = extensions[i];

            if (contains(name, extension.getAttribute("protocolName"))) {
                return extension;
            }
        }

        return null;
    }

    boolean contains(String what, String where) {
        String parts[] = where.split("[, ]");

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals(what)) {
                return true;
            }
        }

        return false;
    }
}
