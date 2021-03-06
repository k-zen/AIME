package io.aime.parse;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

// DOM
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// IO
import java.io.InputStream;

// Log4j
import org.apache.log4j.Logger;

// Net
import java.net.URL;

// SAX
import org.xml.sax.InputSource;

// Util
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// XML
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * A reader to load the information stored in the
 * <code>$AIME_HOME/conf/parse-plugins.xml</code> file.
 *
 * @author Nutch.org
 * @author K-Zen
 */
class ParsePluginsReader {

    /*
     * our log stream
     */
    private static final String KEY = ParsePluginsReader.class.getName();
    private static final Logger LOG = Logger.getLogger(KEY);
    /**
     * The property name of the parse-plugins location
     */
    private static final String PP_FILE_PROP = "parse.plugin.file";
    /**
     * the parse-plugins file
     */
    private String fParsePluginsFile = null;

    /**
     * Constructs a new ParsePluginsReader
     */
    public ParsePluginsReader() {
    }

    /**
     * Reads the
     * <code>parse-plugins.xml</code> file and returns the
     * {@link #ParsePluginList} defined by it.
     *
     * @return A {@link #ParsePluginList} specified by      *         the <code>parse-plugins.xml</code> file.
     *
     * @throws Exception If any parsing error occurs.
     */
    public ParsePluginList parse(Configuration conf) {
        ParsePluginList pList = new ParsePluginList();
        // open up the XML file
        DocumentBuilderFactory factory = null;
        DocumentBuilder parser = null;
        Document document = null;
        InputSource inputSource = null;

        InputStream ppInputStream = null;
        if (fParsePluginsFile != null) {
            URL parsePluginUrl = null;
            try {
                parsePluginUrl = new URL(fParsePluginsFile);
                ppInputStream = parsePluginUrl.openStream();
            }
            catch (Exception e) {
                LOG.warn("Imposible cargar archivo de plugin de parseado desde URL -> " + "[" + fParsePluginsFile + "]. Reason is [" + e + "].");

                return pList;
            }
        }
        else {
            ppInputStream = conf.getConfResourceAsInputStream(conf.get(PP_FILE_PROP));
        }

        inputSource = new InputSource(ppInputStream);

        try {
            factory = DocumentBuilderFactory.newInstance();
            parser = factory.newDocumentBuilder();
            document = parser.parse(inputSource);
        }
        catch (Exception e) {
            LOG.warn("Imposible parsear -> [" + fParsePluginsFile + "]." + "Reason is [" + e + "].");

            return null;
        }

        Element parsePlugins = document.getDocumentElement();
        // build up the alias hash map
        Map<String, String> aliases = getAliases(parsePlugins);
        // And store it on the parse plugin list
        pList.setAliases(aliases);
        // get all the mime type nodes
        NodeList mimeTypes = parsePlugins.getElementsByTagName("mimeType");

        // iterate through the mime types
        for (int i = 0; i < mimeTypes.getLength(); i++) {
            Element mimeType = (Element) mimeTypes.item(i);
            String mimeTypeStr = mimeType.getAttribute("name");
            // for each mimeType, get the plugin list
            NodeList pluginList = mimeType.getElementsByTagName("plugin");

            // iterate through the plugins, add them in order read
            // OR if they have a special order="" attribute, then hold those in
            // a separate list, and then insert them into the final list at the
            // order specified
            if (pluginList != null && pluginList.getLength() > 0) {
                List<String> plugList = new ArrayList<String>(pluginList.getLength());

                for (int j = 0; j < pluginList.getLength(); j++) {
                    Element plugin = (Element) pluginList.item(j);
                    String pluginId = plugin.getAttribute("id");
                    String extId = aliases.get(pluginId);

                    if (extId == null) {
                        // Assume an extension id is directly specified
                        extId = pluginId;
                    }

                    String orderStr = plugin.getAttribute("order");
                    int order = -1;

                    try {
                        order = Integer.parseInt(orderStr);
                    }
                    catch (NumberFormatException ignore) {
                    }

                    if (order != -1) {
                        plugList.add(order - 1, extId);
                    }
                    else {
                        plugList.add(extId);
                    }
                }

                // now add the plugin list and map it to this mimeType
                pList.setPluginList(mimeTypeStr, plugList);

            }
            else {
                LOG.warn("Error! No plugins definidos para [mime type] -> " + mimeTypeStr + ", continuando el parseado.");
            }
        }

        return pList;
    }

    public String getFParsePluginsFile() {
        return fParsePluginsFile;
    }

    public void setFParsePluginsFile(String parsePluginsFile) {
        fParsePluginsFile = parsePluginsFile;
    }

    private Map<String, String> getAliases(Element parsePluginsRoot) {
        Map<String, String> aliases = new HashMap<String, String>();
        NodeList aliasRoot = parsePluginsRoot.getElementsByTagName("aliases");

        if (aliasRoot == null || (aliasRoot != null && aliasRoot.getLength() == 0)) {
            LOG.warn("No aliases definidos en [parse-plugins.xml]!");

            return aliases;
        }

        if (aliasRoot.getLength() > 1) {
            // log a warning, but try and continue processing
            LOG.warn("Debe haber solo una etiqueta \"aliases\" en [parse-plugins.xml].");
        }

        Element aliasRootElem = (Element) aliasRoot.item(0);
        NodeList aliasElements = aliasRootElem.getElementsByTagName("alias");

        if (aliasElements != null && aliasElements.getLength() > 0) {
            for (int i = 0; i < aliasElements.getLength(); i++) {
                Element aliasElem = (Element) aliasElements.item(i);
                String parsePluginId = aliasElem.getAttribute("name");
                String extensionId = aliasElem.getAttribute("extension-id");

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Encontrado alias -> [plugin-id] -> " + parsePluginId + ", [extension-id] -> " + extensionId);
                }

                if (parsePluginId != null && extensionId != null) {
                    aliases.put(parsePluginId, extensionId);
                }
            }
        }

        return aliases;
    }
}
