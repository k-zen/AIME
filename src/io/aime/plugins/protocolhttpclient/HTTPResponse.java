package io.aime.plugins.protocolhttpclient;

// AIME
import io.aime.crawl.CrawlDatum;
import io.aime.metadata.DocMetadata;
import io.aime.metadata.SpellCheckedMetadata;
import io.aime.net.protocols.HTTPDateFormat;
import io.aime.net.protocols.Response;
import io.aime.plugins.libhttp.HTTPBase;

// Apache Commons
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.HttpException;

// IO
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

// Log4j
import org.apache.log4j.Logger;

// Net
import java.net.URL;

/**
 * An HTTP response.
 *
 * @author Susam Pal
 * @author K-Zen
 */
public class HTTPResponse implements Response {

    private static final Logger LOG = Logger.getLogger(HTTPResponse.class.getName());
    private URL url;
    private byte[] content;
    private int code;
    private DocMetadata headers = new SpellCheckedMetadata();

    /**
     * Fetches the given
     * <code>url</code> and prepares HTTP response.
     *
     * @param http            An instance of the implementation class of this
     *                        plugin
     * @param url             URL to be fetched
     * @param datum           Crawl data
     * @param followRedirects Whether to follow redirects; follows redirect if
     *                        and only if this is true
     *
     * @return HTTP response
     *
     * @throws IOException When an error occurs
     */
    HTTPResponse(HTTP http, URL url, CrawlDatum datum, boolean followRedirects) throws IOException {
        // Prepare GET method for HTTP request
        this.url = url;

        GetMethod get = new GetMethod(url.toString());
        get.setFollowRedirects(followRedirects);
        get.setDoAuthentication(true);

        if (datum.getModifiedTime() > 0) {
            get.setRequestHeader("If-Modified-Since", HTTPDateFormat.toString(datum.getModifiedTime()));
        }

        // Set HTTP parameters
        HttpMethodParams params = get.getParams();

        if (http.getUseHttp11()) {
            params.setVersion(HttpVersion.HTTP_1_1);
        }
        else {
            params.setVersion(HttpVersion.HTTP_1_0);
        }

        params.makeLenient();
        params.setContentCharset("UTF-8");
        params.setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        params.setBooleanParameter(HttpMethodParams.SINGLE_COOKIE_HEADER, true);

        // XXX (ab) not sure about this... the default is to retry 3 times; if
        // XXX the request body was sent the method is not retried, so there is
        // XXX little danger in retrying...
        // params.setParameter(HttpMethodParams.RETRY_HANDLER, null);
        try {
            this.code = HTTP.getClient().executeMethod(get);
            Header[] heads = get.getResponseHeaders();

            for (int i = 0; i < heads.length; i++) {
                headers.set(heads[i].getName(), heads[i].getValue());
            }

            // Limit download size
            int contentLength = Integer.MAX_VALUE;
            String contentLengthString = headers.get(Response.CONTENT_LENGTH);

            if (contentLengthString != null) {
                try {
                    contentLength = Integer.parseInt(contentLengthString.trim());
                }
                catch (NumberFormatException e) {
                    throw new HttpException("Content length incorrect: " + contentLengthString);
                }
            }

            if (http.getMaxContent() >= 0 && contentLength > http.getMaxContent()) {
                contentLength = http.getMaxContent();
            }

            // always read content. Sometimes content is useful to find a cause
            // for error.
            InputStream in = get.getResponseBodyAsStream();
            try {
                byte[] buffer = new byte[HTTPBase.BUFFER_SIZE];
                int bufferFilled = 0;
                int totalRead = 0;
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                while ((bufferFilled = in.read(buffer, 0, buffer.length)) != -1 && (totalRead) < contentLength) {
                    totalRead += bufferFilled;
                    out.write(buffer, 0, bufferFilled);
                }

                content = out.toByteArray();
            }
            catch (Exception e) {
                if (code == 200) {
                    throw new IOException(e.toString());
                }
                // for codes other than 200 OK, we are fine with empty content
            }
            finally {
                if (in != null) {
                    in.close();
                }

                get.abort();
            }

            StringBuilder fetchTrace = null;
            fetchTrace = new StringBuilder("URL: " + url + "; Status Code: " + code + "; Bytes received: " + content.length);

            if (this.getHeader(Response.CONTENT_LENGTH) != null) {
                fetchTrace.append("; Content-Length: " + this.getHeader(Response.CONTENT_LENGTH));
            }

            if (this.getHeader(Response.LOCATION) != null) {
                fetchTrace.append("; Location: " + this.getHeader(Response.LOCATION));
            }

            // Extract gzip, x-gzip and deflate content
            if (content != null) {
                // check if we have to uncompress it
                String contentEncoding = headers.get(Response.CONTENT_ENCODING);

                if (contentEncoding != null && true) {
                    fetchTrace.append("; Content-Encoding: " + contentEncoding);
                }

                if ("gzip".equals(contentEncoding) || "x-gzip".equals(contentEncoding)) {
                    content = http.processGzipEncoded(content, url);
                    fetchTrace.append("; extracted to [" + content.length + "] bytes.");
                }
                else if ("deflate".equals(contentEncoding)) {
                    content = http.processDeflateEncoded(content, url);
                    fetchTrace.append("; extracted to [" + content.length + "] bytes.");
                }
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace(fetchTrace.toString());
            }
        }
        finally {
            get.releaseConnection();
        }
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getHeader(String name) {
        return this.headers.get(name);
    }

    @Override
    public DocMetadata getHeaders() {
        return this.headers;
    }

    @Override
    public byte[] getContent() {
        return this.content;
    }
}
