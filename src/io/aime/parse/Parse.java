package io.aime.parse;

/**
 * The result of parsing a page's raw content.
 * @see Parser#getParse(Content)
 */
public interface Parse {

    /**
     * The textual content of the page. This is indexed, searched, and used when
     * generating snippets.
     * @return
     */
    String getText();

    /**
     * Other data extracted from the page.
     * @return
     */
    ParseData getData();

    /**
     * Indicates if the parse is coming from a url or a sub-url.
     * @return
     */
    boolean isCanonical();
}
