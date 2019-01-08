package io.aime.plugins.languageidentifier;

// AIME
import io.aime.metadata.DocMetadata;
import io.aime.parse.HtmlMetaTags;
import io.aime.parse.Parse;
import io.aime.parse.ParseResult;
import io.aime.parse.HtmlParseFilter;
import io.aime.protocol.Content;
import io.aime.util.DOMTreeWalker;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

// DOM
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

// Log4j
import org.apache.log4j.Logger;

// Util
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Adds metadata identifying language of document if found We could also run
 * statistical analysis here but we'd miss all other formats.
 *
 * @author K-Zen
 */
@SuppressWarnings("unchecked")
public class HtmlLanguageParser implements HtmlParseFilter {

    private static final String KEY = HtmlLanguageParser.class.getName();
    private static final Logger LOG = Logger.getLogger(KEY);
    private Configuration conf;

    /*
     * A static Map of ISO-639 language codes
     */
    private static Map LANGUAGES_MAP = new HashMap();

    static {
        try {
            Properties p = new Properties();
            p.load(HtmlLanguageParser.class.getResourceAsStream("/langmappings.properties"));
            Enumeration keys = p.keys();

            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String[] values = p.getProperty(key).split(",", -1);
                LANGUAGES_MAP.put(key, key);

                for (int i = 0; i < values.length; i++) {
                    LANGUAGES_MAP.put(values[i].trim().toLowerCase(), key);
                }
            }
        }
        catch (Exception e) {
            LOG.fatal("Impossible to load language mappings properties file. Error: " + e.toString(), e);
        }
    }

    /**
     * Scan the HTML document looking at possible indications of content
     * language.
     *
     * <ul>
     * <li>1. html lang attribute
     * (http://www.w3.org/TR/REC-html40/struct/dirlang.html#h-8.1)</li>
     * <li>2. meta dc.language
     * (http://dublincore.org/documents/2000/07/16/usageguide/qualified-html.shtml#language)</li>
     * <li>3. meta http-equiv (content-language)
     * (http://www.w3.org/TR/REC-html40/struct/global.html#h-7.4.4.2)</li>
     * </ul>
     *
     * <p>
     * Only the first occurence of language is stored.
     * </p>
     */
    @Override
    public ParseResult filter(Content content, ParseResult parseResult, HtmlMetaTags metaTags, DocumentFragment doc) {
        Parse parse = parseResult.get(content.getUrl());
        String lang = HtmlLanguageParser.getLanguageFromMetadata(parse.getData().getParseMeta());

        if (lang != null) {
            parse.getData().getParseMeta().set(DocMetadata.LANGUAGE, lang);

            return parseResult;
        }

        // Trying to find the document's language
        LanguageParser parser = new LanguageParser(doc);
        lang = parser.getLanguage();

        if (lang != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Language detected through HTML tags. Language: " + lang);
            }
        }

        if (lang != null) {
            parse.getData().getParseMeta().set(DocMetadata.LANGUAGE, lang);
        }

        return parseResult;
    }

    // Check in the metadata whether the language has already been stored there by Tika
    private static String getLanguageFromMetadata(DocMetadata parseMD) {
        // dublin core
        String lang = parseMD.get("dc.language");
        if (lang != null) {
            return lang;
        }

        // meta content-language
        lang = parseMD.get("content-language");
        if (lang != null) {
            return lang;
        }

        // lang attribute
        return parseMD.get("lang");
    }

    static class LanguageParser {

        private String dublinCore = null;
        private String htmlAttribute = null;
        private String httpEquiv = null;
        private String language = null;

        LanguageParser(Node node) {
            this.parse(node);

            if (htmlAttribute != null) {
                language = htmlAttribute;
            }
            else if (dublinCore != null) {
                language = dublinCore;
            }
            else {
                language = httpEquiv;
            }
        }

        String getLanguage() {
            return language;
        }

        void parse(Node node) {
            DOMTreeWalker walker = new DOMTreeWalker(node);

            while (walker.hasNext()) {
                Node currentNode = walker.nextNode();
                String nodeName = currentNode.getNodeName();
                short nodeType = currentNode.getNodeType();
                String lang = null;

                if (nodeType == Node.ELEMENT_NODE) {
                    // Check for the lang HTML attribute
                    if (htmlAttribute == null) {
                        htmlAttribute = LanguageParser.parseLanguage(((Element) currentNode).getAttribute("lang"));
                    }

                    // Check for Meta
                    if ("meta".equalsIgnoreCase(nodeName)) {
                        NamedNodeMap attrs = currentNode.getAttributes();

                        // Check for the dc.language Meta
                        if (dublinCore == null) {
                            for (int i = 0; i < attrs.getLength(); i++) {
                                Node attrnode = attrs.item(i);

                                if ("name".equalsIgnoreCase(attrnode.getNodeName())) {
                                    if ("dc.language".equalsIgnoreCase(attrnode.getNodeValue())) {
                                        Node valueattr = attrs.getNamedItem("content");

                                        if (valueattr != null) {
                                            dublinCore = LanguageParser.parseLanguage(valueattr.getNodeValue());
                                        }
                                    }
                                }
                            }
                        }

                        // Check for the http-equiv content-language
                        if (httpEquiv == null) {
                            for (int i = 0; i < attrs.getLength(); i++) {
                                Node attrnode = attrs.item(i);

                                if ("http-equiv".equalsIgnoreCase(attrnode.getNodeName())) {
                                    if ("content-language".equals(attrnode.getNodeValue().toLowerCase())) {
                                        Node valueattr = attrs.getNamedItem("content");

                                        if (valueattr != null) {
                                            httpEquiv = LanguageParser.parseLanguage(valueattr.getNodeValue());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if ((dublinCore != null) && (htmlAttribute != null) && (httpEquiv != null)) {
                    return;
                }
            }
        }

        /**
         * Parse a language string and return an ISO 639 primary code, or
         * <code>null</code> if something wrong occurs, or if no language is
         * found.
         */
        final static String parseLanguage(String lang) {
            if (lang == null) {
                return null;
            }

            String code = null;
            String language = null;

            // First, split multi-valued values
            String langs[] = lang.split(",| |;|\\.|\\(|\\)|=", -1);

            int i = 0;
            while ((language == null) && (i < langs.length)) {
                // Then, get the primary code
                code = langs[i].split("-")[0];
                code = code.split("_")[0];
                // Find the ISO 639 code
                language = (String) LANGUAGES_MAP.get(code.toLowerCase());
                i++;
            }

            return language;
        }
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    @Override
    public Configuration getConf() {
        return this.conf;
    }
}
