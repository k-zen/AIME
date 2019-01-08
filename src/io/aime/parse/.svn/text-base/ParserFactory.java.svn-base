package io.aime.parse;

// AIME
import io.aime.plugin.Extension;
import io.aime.plugin.ExtensionPoint;
import io.aime.plugin.PluginRuntimeException;
import io.aime.plugin.PluginRepository;
import io.aime.util.MimeUtil;
import io.aime.util.ObjectCache;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

// Log4j
import org.apache.log4j.Logger;

// Util
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Creates and caches {@link Parser} plugins.
 *
 * @author Nutch.org
 * @author K-Zen
 */
public final class ParserFactory {

    private static final Logger LOG = Logger.getLogger(ParserFactory.class.getName());
    /**
     * Wildcard for default plugins.
     */
    public static final String DEFAULT_PLUGIN = "*";
    /**
     * Empty extension list for caching purposes.
     */
    private final List EMPTY_EXTENSION_LIST = Collections.EMPTY_LIST;
    private Configuration conf;
    private ExtensionPoint extensionPoint;
    private ParsePluginList parsePluginList;

    public ParserFactory(Configuration conf) {
        this.conf = conf;

        ObjectCache objectCache = ObjectCache.get(conf);

        this.extensionPoint = PluginRepository.get(conf).getExtensionPoint(Parser.X_POINT_ID);
        this.parsePluginList = (ParsePluginList) objectCache.getObject(ParsePluginList.class.getName());

        if (this.parsePluginList == null) {
            this.parsePluginList = new ParsePluginsReader().parse(conf);
            objectCache.setObject(ParsePluginList.class.getName(), this.parsePluginList);
        }

        if (this.extensionPoint == null) {
            throw new RuntimeException("Point: " + Parser.X_POINT_ID + " not found!");
        }

        if (this.parsePluginList == null) {
            throw new RuntimeException("Parsing preferences not loaded!");
        }
    }

    /**
     * Function returns an array of {@link Parser}s for a given content type.
     *
     * <p>The function consults the internal list of parse plugins for the
     * ParserFactory to determine the list of pluginIds, then gets the
     * appropriate extension points to instantiate as {@link Parser}s.</p>
     *
     * @param contentType The contentType to return the <code>Array</code> of
     *                    {@link Parser}s for.
     * @param url         The url for the content that may allow us to get the
     *                    type from the file suffix.
     *
     * @return An <code>Array</code> of {@link Parser}s for the given
     *         contentType. If there were plugins mapped to a contentType via
     *         the <code>parse-plugins.xml</code> file, but never enabled via
     *         the <code>plugin.includes</code> AIME conf, then those plugins
     *         won't be part of this array, i.e., they will be skipped. So, if
     *         the ordered list of parsing plugins for <code>text/plain</code>
     *         was <code>[parse-text,parse-html,
     *         parse-rtf]</code>, and only <code>parse-html</code> and
     *         <code>parse-rtf</code> were enabled via
     *         <code>plugin.includes</code>, then this ordered Array would
     *         consist of two {@link Parser} interfaces,
     *         <code>[parse-html, parse-rtf]</code>.
     *
     * @throws ParserNotFound
     */
    public Parser[] getParsers(String contentType, String url) throws ParserNotFound {
        List<Parser> parsers = null;
        List<Extension> parserExts = null;
        ObjectCache objectCache = ObjectCache.get(conf);

        // TODO once the MimeTypes is available
        // parsers = getExtensions(MimeUtils.map(contentType));
        // if (parsers != null) {
        //   return parsers;
        // }
        // Last Chance: Guess content-type from file url...
        // parsers = getExtensions(MimeUtils.getMimeType(url));

        parserExts = getExtensions(contentType);
        if (parserExts == null) {
            throw new ParserNotFound(url, contentType);
        }

        parsers = new Vector<Parser>(parserExts.size());
        for (Iterator i = parserExts.iterator(); i.hasNext();) {
            Extension ext = (Extension) i.next();
            Parser p = null;

            try {
                //check to see if we've cached this parser instance yet
                p = (Parser) objectCache.getObject(ext.getId());

                if (p == null) {
                    // go ahead and instantiate it and then cache it
                    p = (Parser) ext.getExtensionInstance();
                    objectCache.setObject(ext.getId(), p);
                }

                parsers.add(p);
            }
            catch (PluginRuntimeException e) {
                LOG.warn("PluginRuntimeException: Starting parsing plugin instance: [" + ext.getDescriptor().getPluginId() + "].", e);
            }
        }

        return parsers.toArray(new Parser[]{});
    }

    /**
     * Function returns a {@link Parser} instance with the specified
     * <code>extId</code>, representing its extension ID.
     *
     * <p>If the Parser instance isn't found, then the function throws a
     * <code>ParserNotFound</code> exception. If the function is able to find
     * the {@link Parser} in the internal
     * <code>PARSER_CACHE</code> then it will return the already instantiated
     * Parser. Otherwise, if it has to instantiate the Parser itself , then this
     * function will cache that Parser in the internal
     * <code>PARSER_CACHE</code>.</p>
     *
     * @param id The string extension ID.
     *
     * @return A {@link Parser} implementation specified by the parameter
     *         <code>id</code>.
     *
     * @throws ParserNotFound If the Parser is not found (i.e., registered with
     *                        the extension point), or if the there a
     *                        {@link PluginRuntimeException} instantiating the
     *                        {@link Parser}.
     */
    public Parser getParserById(String id) throws ParserNotFound {
        Extension[] extensions = this.extensionPoint.getExtensions();
        Extension parserExt = null;
        ObjectCache objectCache = ObjectCache.get(conf);

        if (id != null) {
            parserExt = getExtension(extensions, id);
        }

        if (parserExt == null) {
            parserExt = getExtensionFromAlias(extensions, id);
        }

        if (parserExt == null) {
            throw new ParserNotFound("Parser not found for ID: " + id);
        }

        // first check the cache	    	   
        if (objectCache.getObject(parserExt.getId()) != null) {
            return (Parser) objectCache.getObject(parserExt.getId());
            // if not found in cache, instantiate the Parser
        }
        else {
            try {
                Parser p = (Parser) parserExt.getExtensionInstance();
                objectCache.setObject(parserExt.getId(), p);

                return p;
            }
            catch (PluginRuntimeException e) {
                LOG.warn("Impossible to start parser: " + parserExt.getDescriptor().getPluginId() + ". Error: " + e.toString(), e);
                throw new ParserNotFound("Can't start parser for ID: " + id);
            }
        }
    }

    /**
     * Finds the best-suited parse plugin for a given contentType.
     *
     * @param contentType Content-Type for which we seek a parse plugin.
     *
     * @return a list of extensions to be used for this contentType. If none,
     *         returns <code>null</code>.
     */
    protected List<Extension> getExtensions(String contentType) {
        ObjectCache objectCache = ObjectCache.get(conf);

        // First of all, tries to clean the content-type
        String type = null;
        type = MimeUtil.cleanMimeType(contentType);
        List<Extension> extensions = (List<Extension>) objectCache.getObject(type);

        // Just compare the reference:
        // if this is the empty list, we know we will find no extension.
        if (extensions == EMPTY_EXTENSION_LIST) {
            return null;
        }

        if (extensions == null) {
            extensions = findExtensions(type);
            if (extensions != null) {
                objectCache.setObject(type, extensions);
            }
            else {
                // Put the empty extension list into cache
                // to remember we don't know any related extension.
                objectCache.setObject(type, EMPTY_EXTENSION_LIST);
            }
        }

        return extensions;
    }

    /**
     * searches a list of suitable parse plugins for the given contentType.
     * <p>It first looks for a preferred plugin defined in the parse-plugin
     * file. If none is found, it returns a list of default plugins.
     *
     * @param contentType Content-Type for which we seek a parse plugin.
     *
     * @return List - List of extensions to be used for this contentType. If
     *         none, returns null.
     */
    private List<Extension> findExtensions(String contentType) {
        Extension[] extensions = this.extensionPoint.getExtensions();

        // Look for a preferred plugin.
        List<String> pPluginList = this.parsePluginList.getPluginList(contentType);
        List<Extension> extensionList = matchExtensions(pPluginList, extensions, contentType);

        if (extensionList != null) {
            return extensionList;
        }

        // If none found, look for a default plugin.
        pPluginList = this.parsePluginList.getPluginList(DEFAULT_PLUGIN);

        return matchExtensions(pPluginList, extensions, DEFAULT_PLUGIN);
    }

    /**
     * Tries to find a suitable parser for the given contentType.
     * <ol>
     * <li>It checks if a parser which accepts the contentType can be found in
     * the
     * <code>plugins</code> list;</li>
     * <li>If this list is empty, it tries to find amongst the loaded extensions
     * whether some of them might suit and warns the user.</li>
     * </ol>
     *
     * @param plugins     List of candidate plugins.
     * @param extensions  Array of loaded extensions.
     * @param contentType Content-Type for which we seek a parse plugin.
     *
     * @return List - List of extensions to be used for this contentType. If
     *         none, returns null.
     */
    private List<Extension> matchExtensions(List<String> plugins, Extension[] extensions, String contentType) {
        List<Extension> extList = new ArrayList<Extension>();

        if (plugins != null) {
            for (String parsePluginId : plugins) {
                Extension ext = getExtension(extensions, parsePluginId, contentType);
                // the extension returned may be null
                // that means that it was not enabled in the plugin.includes
                // AIME conf property, but it was mapped in the
                // parse-plugins.xml
                // file. 
                // OR it was enabled in plugin.includes, but the plugin's plugin.xml
                // file does not claim that the plugin supports the specified mimeType
                // in either case, LOG the appropriate error message to WARN level

                if (ext == null) {
                    //try to get it just by its pluginId
                    ext = getExtension(extensions, parsePluginId);

                    if (ext != null) {
                        // plugin was enabled via plugin.includes
                        // its plugin.xml just doesn't claim to support that
                        // particular mimeType
                        LOG.warn("Plugin: " + parsePluginId + " set to: " + contentType + ", but the plugin doesn't support: " + contentType);
                    }
                    else {
                        // plugin wasn't enabled via plugin.includes
                        LOG.warn("Plugin: " + parsePluginId + " set to: " + contentType + ", but not enabled via plugin.includes in aime-site.xml.");
                    }
                }

                if (ext != null) {
                    // add it to the list
                    extList.add(ext);
                }
            }

        }
        else {
            // okay, there were no list of plugins defined for
            // this mimeType, however, there may be plugins registered
            // via the plugin.includes AIME conf property that claim
            // via their plugin.xml file to support this contentType
            // so, iterate through the list of extensions and if you find
            // any extensions where this is the case, throw a
            // NotMappedParserException
            for (int i = 0; i < extensions.length; i++) {
                if (extensions[i].getAttribute("contentType") != null && extensions[i].getAttribute("contentType").equals(contentType)) {
                    extList.add(extensions[i]);
                }
                else if ("*".equals(extensions[i].getAttribute("contentType"))) {
                    extList.add(0, extensions[i]);
                }
            }

            if (extList.size() > 0) {
                StringBuilder extensionsIDs = new StringBuilder("[");
                boolean isFirst = true;

                for (Extension ext : extList) {
                    if (!isFirst) {
                        extensionsIDs.append(" - ");
                    }
                    else {
                        isFirst = false;
                    }
                    extensionsIDs.append(ext.getId());
                }

                extensionsIDs.append("]");
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Using default parsing plugin: " + extensionsIDs.toString() + " for " + contentType);
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("No active parsing plugins for: " + contentType);
            }
        }

        return (extList.size() > 0) ? extList : null;
    }

    private boolean match(Extension extension, String id, String type) {
        return ((id.equals(extension.getId()))
                && (type.equals(extension.getAttribute("contentType"))
                    || extension.getAttribute("contentType").equals("*")
                    || type.equals(DEFAULT_PLUGIN)));
    }

    /**
     * Get an extension from its id and supported content-type.
     */
    private Extension getExtension(Extension[] list, String id, String type) {
        for (int i = 0; i < list.length; i++) {
            if (match(list[i], id, type)) {
                return list[i];
            }
        }

        return null;
    }

    private Extension getExtension(Extension[] list, String id) {
        for (int i = 0; i < list.length; i++) {
            if (id.equals(list[i].getId())) {
                return list[i];
            }
        }

        return null;
    }

    private Extension getExtensionFromAlias(Extension[] list, String id) {
        return getExtension(list, parsePluginList.getAliases().get(id));
    }
}
