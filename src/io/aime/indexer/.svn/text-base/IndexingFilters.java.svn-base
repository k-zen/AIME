package io.aime.indexer;

// AIME
import io.aime.crawl.CrawlDatum;
import io.aime.crawl.Inlinks;
import io.aime.parse.Parse;
import io.aime.plugin.Extension;
import io.aime.plugin.ExtensionPoint;
import io.aime.plugin.PluginRepository;
import io.aime.plugin.PluginRuntimeException;
import io.aime.util.ObjectCache;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;

// Log4j
import org.apache.log4j.Logger;

// Util
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Creates and caches {@link IndexingFilter} implementing plugins.
 *
 * @author Nutch.org
 * @author K-Zen
 */
public class IndexingFilters {

    public static final String INDEXINGFILTER_ORDER = "indexingfilter.order";
    private static final String KEY = IndexingFilters.class.getName();
    private static final Logger LOG = Logger.getLogger(KEY);
    private IndexingFilter[] indexingFilters;

    public IndexingFilters(Configuration conf) {
        /*
         * Get indexingfilter.order property
         */
        String order = conf.get(INDEXINGFILTER_ORDER);
        ObjectCache objectCache = ObjectCache.get(conf);
        this.indexingFilters = (IndexingFilter[]) objectCache.getObject(IndexingFilter.class.getName());

        if (this.indexingFilters == null) {
            // If ordered filters are required, prepare array of filters based on property.
            String[] orderedFilters = null;

            if (order != null && !order.trim().equals("")) {
                orderedFilters = order.split("\\s+");
            }

            try {
                ExtensionPoint point = PluginRepository.get(conf).getExtensionPoint(IndexingFilter.X_POINT_ID);

                if (point == null) {
                    throw new RuntimeException(IndexingFilter.X_POINT_ID + " not found.");
                }

                Extension[] extensions = point.getExtensions();
                HashMap<String, IndexingFilter> filterMap = new HashMap<String, IndexingFilter>();

                for (int i = 0; i < extensions.length; i++) {
                    Extension extension = extensions[i];
                    IndexingFilter filter = (IndexingFilter) extension.getExtensionInstance();

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Adding: " + filter.getClass().getName());
                    }

                    if (!filterMap.containsKey(filter.getClass().getName())) {
                        filter.addIndexBackendOptions(conf);
                        filterMap.put(filter.getClass().getName(), filter);
                    }
                }

                /*
                 * If no ordered filters required, just get the filters in an
                 * indeterminate order
                 */
                if (orderedFilters == null) {
                    objectCache.setObject(IndexingFilter.class.getName(), filterMap.values().toArray(new IndexingFilter[0]));
                    /*
                     * Otherwise run the filters in the required order
                     */
                }
                else {
                    ArrayList<IndexingFilter> filters = new ArrayList<IndexingFilter>();

                    for (int i = 0; i < orderedFilters.length; i++) {
                        IndexingFilter filter = filterMap.get(orderedFilters[i]);

                        if (filter != null) {
                            filter.addIndexBackendOptions(conf);
                            filters.add(filter);
                        }
                    }

                    objectCache.setObject(IndexingFilter.class.getName(), filters.toArray(new IndexingFilter[filters.size()]));
                }
            }
            catch (PluginRuntimeException e) {
                throw new RuntimeException(e);
            }

            this.indexingFilters = (IndexingFilter[]) objectCache.getObject(IndexingFilter.class.getName());
        }
    }

    /**
     * Run all defined filters.
     *
     * @param doc
     * @param parse
     * @param url
     * @param datum
     * @param inlinks
     *
     * @return
     *
     * @throws IndexingException
     */
    public AIMEDocument filter(AIMEDocument doc, Parse parse, Text url, CrawlDatum datum, Inlinks inlinks) throws IndexingException {
        for (int i = 0; i < this.indexingFilters.length; i++) {
            doc = this.indexingFilters[i].filter(doc, parse, url, datum, inlinks);

            // break the loop if an indexing filter discards the doc
            if (doc == null) {
                return null;
            }
        }

        return doc;
    }

    public void close() {
        for (int i = 0; i < this.indexingFilters.length; i++) {
            this.indexingFilters[i].close();
        }
    }
}
