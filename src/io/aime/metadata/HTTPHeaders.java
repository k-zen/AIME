package io.aime.metadata;

/**
 * A collection of HTTP header names.
 *
 * @see <a href="http://rfc-ref.org/RFC-TEXTS/2616/">Hypertext Transfer Protocol
 * -- HTTP/1.1 (RFC 2616)</a>
 *
 * @author Chris Mattmann
 * @author J&eacute;r&ocirc;me Charron
 * @author K-Zen
 */
public interface HTTPHeaders {

    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String CONTENT_LANGUAGE = "Content-Language";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_LOCATION = "Content-Location";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String CONTENT_MD5 = "Content-MD5";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String LAST_MODIFIED = "Last-Modified";
    public static final String LOCATION = "Location";
}
