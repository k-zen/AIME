package io.aime.net;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

// AIME
import io.aime.plugin.Extension;
import io.aime.plugin.ExtensionPoint;
import io.aime.plugin.PluginRuntimeException;
import io.aime.plugin.PluginRepository;
import io.aime.util.AIMEConstants;
import io.aime.util.ObjectCache;

// Util
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates and caches {@link URLFilter} implementing plugins.
 *
 * @author K-Zen
 */
public class URLFilters {

    private URLFilter[] filters;

    /**
     * This constructor initializes the filters and caches them.
     *
     * @param conf Configuration's object.
     */
    public URLFilters(Configuration conf) {
        String order = conf.get(AIMEConstants.URLFILTER_ORDER.getStringConstant()); // The order of the filters.

        // Grab the filters from cache.
        ObjectCache objectCache = ObjectCache.get(conf);
        this.filters = (URLFilter[]) objectCache.getObject(URLFilter.class.getName());
        if (this.filters == null) {
            String[] orderedFilters = null;
            if (order != null && !order.trim().isEmpty()) {
                orderedFilters = order.split("\\s+");
            }

            try {
                ExtensionPoint point = PluginRepository.get(conf).getExtensionPoint(URLFilter.X_POINT_ID);
                if (point == null) {
                    throw new RuntimeException(URLFilter.X_POINT_ID + " not found.");
                }

                Extension[] extensions = point.getExtensions();
                Map<String, URLFilter> filterMap = new HashMap<String, URLFilter>();
                for (int i = 0; i < extensions.length; i++) {
                    Extension extension = extensions[i];
                    URLFilter filter = ((URLFilter) extension.getExtensionInstance()).init();

                    if (!filterMap.containsKey(filter.getClass().getName())) {
                        filterMap.put(filter.getClass().getName(), filter);
                    }
                }

                if (orderedFilters == null) {
                    objectCache.setObject(URLFilter.class.getName(), filterMap.values().toArray(new URLFilter[0]));
                }
                else {
                    ArrayList<URLFilter> f = new ArrayList<URLFilter>();
                    for (int i = 0; i < orderedFilters.length; i++) {
                        URLFilter filter = filterMap.get(orderedFilters[i]);

                        if (filter != null) {
                            f.add(filter);
                        }
                    }

                    objectCache.setObject(URLFilter.class.getName(), f.toArray(new URLFilter[f.size()]));
                }
            }
            catch (PluginRuntimeException e) {
                throw new RuntimeException(e);
            }

            this.filters = (URLFilter[]) objectCache.getObject(URLFilter.class.getName());
        }
    }

    /**
     * Run all defined filters.
     * <p>Assume logical AND.</p>
     *
     * @param url The URL to be filtered.
     *
     * @return The filtered URL.
     *
     * @throws URLFilterException If there was a problem filtering the URL.
     */
    public static String filter(URLFilter[] filters, String url) throws URLFilterException {
        for (int i = 0; i < filters.length; i++) {
            if (url == null) {
                return null;
            }

            url = filters[i].filter(url);
        }

        return url;
    }

    /**
     * Return all active filters.
     *
     * @return An array of active filters.
     */
    public URLFilter[] getFilters() {
        return this.filters;
    }
}
