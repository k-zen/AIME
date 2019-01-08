package io.aime.parse;

// AIME
import io.aime.plugin.PluginRepository;
import io.aime.plugin.PluginRuntimeException;
import io.aime.plugin.Extension;
import io.aime.plugin.ExtensionPoint;
import io.aime.protocol.Content;
import io.aime.util.ObjectCache;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

// DOM
import org.w3c.dom.DocumentFragment;

// Util
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Creates and caches {@link HtmlParseFilter} implementing plugins.
 */
public class HtmlParseFilters {

    private HtmlParseFilter[] htmlParseFilters;
    public static final String HTMLPARSEFILTER_ORDER = "htmlparsefilter.order";

    public HtmlParseFilters(Configuration conf) {
        String order = conf.get(HTMLPARSEFILTER_ORDER);
        ObjectCache objectCache = ObjectCache.get(conf);
        this.htmlParseFilters = (HtmlParseFilter[]) objectCache.getObject(HtmlParseFilter.class.getName());

        if (htmlParseFilters == null) {
            /*
             * If ordered filters are required, prepare array of filters based
             * on property
             */
            String[] orderedFilters = null;

            if (order != null && !order.trim().equals("")) {
                orderedFilters = order.split("\\s+");
            }

            HashMap<String, HtmlParseFilter> filterMap = new HashMap<String, HtmlParseFilter>();

            try {
                ExtensionPoint point = PluginRepository.get(conf).getExtensionPoint(HtmlParseFilter.X_POINT_ID);

                if (point == null) {
                    throw new RuntimeException(HtmlParseFilter.X_POINT_ID + " not found.");
                }

                Extension[] extensions = point.getExtensions();

                for (int i = 0; i < extensions.length; i++) {
                    Extension extension = extensions[i];
                    HtmlParseFilter parseFilter = (HtmlParseFilter) extension.getExtensionInstance();

                    if (!filterMap.containsKey(parseFilter.getClass().getName())) {
                        filterMap.put(parseFilter.getClass().getName(), parseFilter);
                    }
                }

                HtmlParseFilter[] hParseFilters = filterMap.values().toArray(new HtmlParseFilter[filterMap.size()]);

                /*
                 * If no ordered filters required, just get the filters in an
                 * indeterminate order
                 */
                if (orderedFilters == null) {
                    objectCache.setObject(HtmlParseFilter.class.getName(), hParseFilters);
                }
                /*
                 * Otherwise run the filters in the required order
                 */
                else {
                    ArrayList<HtmlParseFilter> filters = new ArrayList<HtmlParseFilter>();

                    for (int i = 0; i < orderedFilters.length; i++) {
                        HtmlParseFilter filter = filterMap.get(orderedFilters[i]);

                        if (filter != null) {
                            filters.add(filter);
                        }
                    }

                    objectCache.setObject(HtmlParseFilter.class.getName(), filters.toArray(new HtmlParseFilter[filters.size()]));
                }
            }
            catch (PluginRuntimeException e) {
                throw new RuntimeException(e);
            }

            this.htmlParseFilters = (HtmlParseFilter[]) objectCache.getObject(HtmlParseFilter.class.getName());
        }
    }

    /**
     * Run all defined filters.
     */
    public ParseResult filter(Content content, ParseResult parseResult, HtmlMetaTags metaTags, DocumentFragment doc) {
        // loop on each filter
        for (int i = 0; i < this.htmlParseFilters.length; i++) {
            // call filter interface
            parseResult = htmlParseFilters[i].filter(content, parseResult, metaTags, doc);

            // any failure on parse obj, return
            if (!parseResult.isSuccess()) {
                // TODO: What happens when parseResult.isEmpty() ?
                // Maybe clone parseResult and use parseResult as backup...
                // remove failed parse before return
                parseResult.filter();

                return parseResult;
            }
        }

        return parseResult;
    }
}
