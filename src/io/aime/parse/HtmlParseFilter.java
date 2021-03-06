package io.aime.parse;

// AIME
import io.aime.plugin.Pluggable;
import io.aime.protocol.Content;

// Apache Hadoop
import org.apache.hadoop.conf.Configurable;

// DOM
import org.w3c.dom.DocumentFragment;

/**
 * Extension point for DOM-based HTML parsers.
 *
 * <p>Permits one to add additional metadata to HTML parses. All plugins found
 * which implement this extension point are run sequentially on the parse.</p>
 */
public interface HtmlParseFilter extends Pluggable, Configurable {

    /**
     * The name of the extension point.
     */
    final static String X_POINT_ID = HtmlParseFilter.class.getName();

    /**
     * Adds metadata or otherwise modifies a parse of HTML content, given the
     * DOM tree of a page.
     */
    ParseResult filter(Content content, ParseResult parseResult, HtmlMetaTags metaTags, DocumentFragment doc);
}
