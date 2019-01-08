package io.aime.net.protocols;

// AIME
import io.aime.metadata.HTTPHeaders;
import io.aime.metadata.DocMetadata;

// Net
import java.net.URL;

/**
 * A response interface. Makes all protocols model HTTP.
 */
public interface Response extends HTTPHeaders {

    /**
     * Returns the URL used to retrieve this response.
     */
    public URL getUrl();

    /**
     * Returns the response code.
     */
    public int getCode();

    /**
     * Returns the value of a named header.
     */
    public String getHeader(String name);

    /**
     * Returns all the headers.
     */
    public DocMetadata getHeaders();

    /**
     * Returns the full content of the response.
     */
    public byte[] getContent();
}
