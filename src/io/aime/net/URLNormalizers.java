package io.aime.net;

// AIME
import io.aime.plugin.Extension;
import io.aime.plugin.ExtensionPoint;
import io.aime.plugin.PluginRepository;
import io.aime.plugin.PluginRuntimeException;
import io.aime.util.ObjectCache;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

// Log4j
import org.apache.log4j.Logger;

// Net
import java.net.MalformedURLException;

// Util
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * This class uses a "chained filter" pattern to run defined normalizers.
 *
 * <p>Different lists of normalizers may be defined for different "scopes", or
 * contexts where they are used (note however that they need to be activated
 * first through <tt>plugin.include</tt> property).</p>
 *
 * <p>There is one global scope defined by default, which consists of all active
 * normalizers. The order in which these normalizers are executed may be defined
 * in "urlnormalizer.order" property, which lists space-separated implementation
 * classes (if this property is missing normalizers will be run in random
 * order). If there are more normalizers activated than explicitly named on this
 * list, the remaining ones will be run in random order after the ones specified
 * on the list are executed.</p>
 *
 * <p>You can define a set of contexts (or scopes) in which normalizers may be
 * called. Each scope can have its own list of normalizers (defined in
 * "urlnormalizer.scope.<scope_name>" property) and its own order (defined in
 * "urlnormalizer.order.<scope_name>" property). If any of these properties are
 * missing, default settings are used for the global scope.</p>
 *
 * <p>In case no normalizers are required for any given scope, a
 * <code>io.aime.plugins.urlnormalizerbasic.BasicURLNormalizer</code> should be
 * used.</p>
 *
 * <p>Each normalizer may further select among many configurations, depending on
 * the scope in which it is called, because the scope name is passed as a
 * parameter to each normalizer. You can also use the same normalizer for many
 * scopes.</p>
 *
 * <p>Several scopes have been defined, and various AIME tools will attempt
 * using scope-specific normalizers first (and fall back to default config if
 * scope-specific configuration is missing).</p>
 *
 * <p>Normalizers may be run several times, to ensure that modifications
 * introduced by normalizers at the end of the list can be further reduced by
 * normalizers executed at the beginning. By default this loop is executed just
 * once - if you want to ensure that all possible combinations have been applied
 * you may want to run this loop up to the number of activated normalizers. This
 * loop count can be configured through <tt>urlnormalizer.loop.count</tt>
 * property. As soon as the url is unchanged the loop will stop and return the
 * result.</p>
 *
 * @author Andrzej Bialecki
 * @author K-Zen
 */
public final class URLNormalizers {

    /**
     * Default scope. If no scope properties are defined then the configuration
     * for this scope will be used.
     */
    public static final String SCOPE_DEFAULT = "default";
    /**
     * Scope used by {@link io.aime.crawl.URLPartitioner}.
     */
    public static final String SCOPE_PARTITION = "partition";
    /**
     * Scope used by {@link io.aime.crawl.Generator}.
     */
    public static final String SCOPE_GENERATE_HOST_COUNT = "generate_host_count";
    /**
     * Scope used by {@link io.aime.fetcher.Fetcher} when processing redirect
     * URLs.
     */
    public static final String SCOPE_FETCHER = "fetcher";
    /**
     * Scope used when updating the CrawlDb with new URLs.
     */
    public static final String SCOPE_CRAWLDB = "crawldb";
    /**
     * Scope used when updating the LinkDb with new URLs.
     */
    public static final String SCOPE_LINKDB = "linkdb";
    /**
     * Scope used by {@link io.aime.crawl.Injector}.
     */
    public static final String SCOPE_INJECT = "inject";
    /**
     * Scope used when constructing new {@link io.aime.parse.Outlink} instances.
     */
    public static final String SCOPE_OUTLINK = "outlink";
    private static final String KEY = URLNormalizers.class.getName();
    private static final Logger LOG = Logger.getLogger(KEY);
    /**
     * Empty extension list for caching purposes.
     */
    private final List<Extension> EMPTY_EXTENSION_LIST = Collections.EMPTY_LIST;
    private final URLNormalizer[] EMPTY_NORMALIZERS = new URLNormalizer[0];
    private Configuration conf;
    private ExtensionPoint extensionPoint;
    private URLNormalizer[] normalizers;
    private int loopCount;

    public URLNormalizers(Configuration conf, String scope) {
        this.conf = conf;
        this.extensionPoint = PluginRepository.get(conf).getExtensionPoint(URLNormalizer.X_POINT_ID);
        ObjectCache objectCache = ObjectCache.get(conf);

        if (this.extensionPoint == null) {
            throw new RuntimeException("Punto X -> " + URLNormalizer.X_POINT_ID + " no encontrado.");
        }

        this.normalizers = (URLNormalizer[]) objectCache.getObject(URLNormalizer.X_POINT_ID + "_" + scope);

        if (this.normalizers == null) {
            this.normalizers = this.getURLNormalizers(scope);
        }
        if (this.normalizers == EMPTY_NORMALIZERS) {
            this.normalizers = (URLNormalizer[]) objectCache.getObject(URLNormalizer.X_POINT_ID + "_" + SCOPE_DEFAULT);

            if (this.normalizers == null) {
                this.normalizers = this.getURLNormalizers(SCOPE_DEFAULT);
            }
        }

        this.loopCount = conf.getInt("urlnormalizer.loop.count", 1);
    }

    /**
     * Function returns an array of {@link URLNormalizer}s for a given scope,
     * with a specified order.
     *
     * @param scope The scope to return the <code>Array</code> of
     *              {@link URLNormalizer}s for.
     *
     * @return An <code>Array</code> of {@link URLNormalizer}s for the given
     *         scope.
     *
     * @throws PluginRuntimeException
     */
    URLNormalizer[] getURLNormalizers(String scope) {
        List<Extension> extensions = this.getExtensions(scope);
        ObjectCache objectCache = ObjectCache.get(conf);

        if (extensions == EMPTY_EXTENSION_LIST) {
            return EMPTY_NORMALIZERS;
        }

        List<URLNormalizer> n = new Vector<URLNormalizer>(extensions.size());

        Iterator<Extension> it = extensions.iterator();
        while (it.hasNext()) {
            Extension ext = it.next();
            URLNormalizer normalizer = null;
            try {
                // check to see if we've cached this URLNormalizer instance yet
                normalizer = (URLNormalizer) objectCache.getObject(ext.getId());

                if (normalizer == null) {
                    // go ahead and instantiate it and then cache it
                    normalizer = (URLNormalizer) ext.getExtensionInstance();
                    objectCache.setObject(ext.getId(), normalizer);
                }

                n.add(normalizer);
            }
            catch (PluginRuntimeException e) {
                LOG.warn("PluginRuntimeException when starting plugin instance: " + ext.getDescriptor().getPluginId() + ". Error: " + e.toString(), e);
            }
        }

        return n.toArray(new URLNormalizer[n.size()]);
    }

    /**
     * Finds the best-suited normalizer plugin for a given scope.
     *
     * @param scope Scope for which we seek a normalizer plugin.
     *
     * @return A list of extensions to be used for this scope. If none, returns
     *         empty list.
     *
     * @throws PluginRuntimeException
     */
    private List<Extension> getExtensions(String scope) {
        ObjectCache objectCache = ObjectCache.get(conf);
        @SuppressWarnings("unchecked")
        List<Extension> extensions = (List<Extension>) objectCache.getObject(URLNormalizer.X_POINT_ID + "_x_" + scope);

        // Just compare the reference:
        // if this is the empty list, we know we will find no extension.
        if (extensions == EMPTY_EXTENSION_LIST) {
            return EMPTY_EXTENSION_LIST;
        }

        if (extensions == null) {
            extensions = this.findExtensions(scope);

            if (extensions != null) {
                objectCache.setObject(URLNormalizer.X_POINT_ID + "_x_" + scope, extensions);
            }
            else {
                // Put the empty extension list into cache
                // to remember we don't know any related extension.
                objectCache.setObject(URLNormalizer.X_POINT_ID + "_x_" + scope, EMPTY_EXTENSION_LIST);
                extensions = EMPTY_EXTENSION_LIST;
            }
        }

        return extensions;
    }

    /**
     * Searches a list of suitable url normalizer plugins for the given scope.
     *
     * @param scope Scope for which we seek a url normalizer plugin.
     *
     * @return List of extensions to be used for this scope. If none, returns
     *         null.
     *
     * @throws PluginRuntimeException
     */
    private List<Extension> findExtensions(String scope) {
        String[] orders = null;
        String orderlist = this.conf.get("urlnormalizer.order." + scope);

        if (orderlist == null) {
            orderlist = this.conf.get("urlnormalizer.order");
        }

        if (orderlist != null && !orderlist.trim().equals("")) {
            orders = orderlist.split("\\s+");
        }

        String scopelist = this.conf.get("urlnormalizer.scope." + scope);
        Set<String> impls = null;

        if (scopelist != null && !scopelist.trim().equals("")) {
            String[] names = scopelist.split("\\s+");
            impls = new HashSet<String>(Arrays.asList(names));
        }

        Extension[] extensions = this.extensionPoint.getExtensions();
        HashMap<String, Extension> normalizerExtensions = new HashMap<String, Extension>();

        for (int i = 0; i < extensions.length; i++) {
            Extension extension = extensions[i];

            if (impls != null && !impls.contains(extension.getClazz())) {
                continue;
            }

            normalizerExtensions.put(extension.getClazz(), extension);
        }

        List<Extension> res = new ArrayList<Extension>();

        if (orders == null) {
            res.addAll(normalizerExtensions.values());
        }
        else {
            // first add those explicitly named in correct order
            for (int i = 0; i < orders.length; i++) {
                Extension e = normalizerExtensions.get(orders[i]);

                if (e != null) {
                    res.add(e);
                    normalizerExtensions.remove(orders[i]);
                }
            }
            // then add all others in random order
            res.addAll(normalizerExtensions.values());
        }

        return res;
    }

    /**
     * Normalize the URL.
     *
     * @param urlString The URL string to normalize.
     * @param scope     The given scope.
     *
     * @return A normalized String, using the given <code>scope</code>.
     *
     * @throws MalformedURLException If the given URL string is malformed.
     */
    public String normalize(String urlString, String scope) throws MalformedURLException {
        // optionally loop several times, and break if no further changes
        String initialString = urlString;

        for (int k = 0; k < loopCount; k++) {
            for (int i = 0; i < this.normalizers.length; i++) {
                if (urlString == null) {
                    return null;
                }

                urlString = this.normalizers[i].normalize(urlString, scope);
            }

            if (initialString.equals(urlString)) {
                break;
            }

            initialString = urlString;
        }

        return urlString;
    }
}
