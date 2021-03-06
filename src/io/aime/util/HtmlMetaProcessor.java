package io.aime.util;

// AIME
import io.aime.parse.HtmlMetaTags;

// DOM
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// Net
import java.net.URL;

/**
 * Class for parsing META Directives from DOM trees.
 *
 * <p>This class handles specifically Robots META directives (all, none,
 * nofollow, noindex), finding BASE HREF tags, and HTTP-EQUIV no-cache
 * instructions.</p>
 *
 * <p>All meta directives are stored in a HTMLMetaTags instance. Utility class
 * with indicators for the robots directives "noindex" and "nofollow", and
 * HTTP-EQUIV/no-cache.</p>
 *
 * @author Nutch.org
 * @author K-Zen
 */
public class HtmlMetaProcessor {

    /**
     * Sets the indicators in robotsMeta to appropriate values, based on any
     * META tags found under the given node.
     */
    public static final void getMetaTags(HtmlMetaTags metaTags, Node node, URL currURL) {
        metaTags.reset();
        HtmlMetaProcessor.getMetaTagsHelper(metaTags, node, currURL);
    }

    private static final void getMetaTagsHelper(HtmlMetaTags metaTags, Node node, URL currURL) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            if ("body".equalsIgnoreCase(node.getNodeName())) {
                // META tags should not be under body
                return;
            }

            if ("meta".equalsIgnoreCase(node.getNodeName())) {
                NamedNodeMap attrs = node.getAttributes();
                Node nameNode = null;
                Node equivNode = null;
                Node contentNode = null;

                // Retrieves name, http-equiv and content attribues
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node attr = attrs.item(i);
                    String attrName = attr.getNodeName().toLowerCase();

                    if (attrName.equals("name")) {
                        nameNode = attr;
                    }
                    else if (attrName.equals("http-equiv")) {
                        equivNode = attr;
                    }
                    else if (attrName.equals("content")) {
                        contentNode = attr;
                    }
                }

                if (nameNode != null) {
                    if (contentNode != null) {
                        String name = nameNode.getNodeValue().toLowerCase();
                        metaTags.getGeneralTags().setProperty(name, contentNode.getNodeValue());

                        if ("robots".equals(name)) {
                            if (contentNode != null) {
                                String directives = contentNode.getNodeValue().toLowerCase();
                                int index = directives.indexOf("none");

                                if (index >= 0) {
                                    metaTags.setNoIndex();
                                    metaTags.setNoFollow();
                                }

                                index = directives.indexOf("all");
                                if (index >= 0) {
                                    // do nothing...
                                }

                                index = directives.indexOf("noindex");
                                if (index >= 0) {
                                    metaTags.setNoIndex();
                                }

                                index = directives.indexOf("nofollow");
                                if (index >= 0) {
                                    metaTags.setNoFollow();
                                }

                                index = directives.indexOf("noarchive");
                                if (index >= 0) {
                                    metaTags.setNoCache();
                                }
                            }
                        } // end if (name == robots)
                    }
                }

                if (equivNode != null) {
                    if (contentNode != null) {
                        String name = equivNode.getNodeValue().toLowerCase();
                        String content = contentNode.getNodeValue();
                        metaTags.getHttpEquivTags().setProperty(name, content);

                        if ("pragma".equals(name)) {
                            content = content.toLowerCase();
                            int index = content.indexOf("no-cache");

                            if (index >= 0) {
                                metaTags.setNoCache();
                            }
                        }
                        else if ("refresh".equals(name)) {
                            int idx = content.indexOf(';');
                            String time = null;

                            if (idx == -1) { // just the refresh time
                                time = content;
                            }
                            else {
                                time = content.substring(0, idx);
                            }

                            try {
                                metaTags.setRefreshTime(Integer.parseInt(time));
                                // skip this if we couldn't parse the time
                                metaTags.setRefresh(true);
                            }
                            catch (Exception e) {
                                // Hacer algo.
                            }

                            URL refreshUrl = null;
                            if (metaTags.getRefresh() && idx != -1) { // set the URL
                                idx = content.toLowerCase().indexOf("url=");

                                if (idx == -1) { // assume a mis-formatted entry with just the url
                                    idx = content.indexOf(';') + 1;
                                }
                                else {
                                    idx += 4;
                                }

                                if (idx != -1) {
                                    String url = content.substring(idx);

                                    try {
                                        refreshUrl = new URL(url);
                                    }
                                    catch (Exception e) {
                                        // XXX according to the spec, this has to be an absolute
                                        // XXX url. However, many websites use relative URLs and
                                        // XXX expect browsers to handle that.
                                        // XXX Unfortunately, in some cases this may create a
                                        // XXX infinitely recursive paths (a crawler trap)...
                                        // if (!url.startsWith("/")) url = "/" + url;
                                        try {
                                            refreshUrl = new URL(currURL, url);
                                        }
                                        catch (Exception e1) {
                                            refreshUrl = null;
                                        }
                                    }
                                }
                            }

                            if (metaTags.getRefresh()) {
                                if (refreshUrl == null) {
                                    // apparently only refresh time was present. set the URL
                                    // to the same URL.
                                    refreshUrl = currURL;
                                }

                                metaTags.setRefreshHref(refreshUrl);
                            }
                        }
                    }
                }
            }
            else if ("base".equalsIgnoreCase(node.getNodeName())) {
                NamedNodeMap attrs = node.getAttributes();
                Node hrefNode = attrs.getNamedItem("href");

                if (hrefNode != null) {
                    String urlString = hrefNode.getNodeValue();
                    URL url = null;

                    try {
                        if (currURL == null) {
                            url = new URL(urlString);
                        }
                        else {
                            url = new URL(currURL, urlString);
                        }
                    }
                    catch (Exception e) {
                        // Hacer algo.
                    }

                    if (url != null) {
                        metaTags.setBaseHref(url);
                    }
                }
            }
        }

        NodeList children = node.getChildNodes();
        if (children != null) {
            int len = children.getLength();
            for (int i = 0; i < len; i++) {
                HtmlMetaProcessor.getMetaTagsHelper(metaTags, children.item(i), currURL);
            }
        }
    }
}
