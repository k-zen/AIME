package io.aime.plugins.protocolsmb;

// AIME
import io.aime.crawl.CrawlDatum;
import io.aime.protocol.Content;
import io.aime.util.MimeUtil;
import io.aime.metadata.DocMetadata;
import io.aime.net.protocols.HTTPDateFormat;
import io.aime.net.protocols.Response;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

// IO
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

// JCIFS
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

// Net
import java.net.URL;

/**
 * SambaResponse.java mimics samba replies as http response.
 *
 * @author K-Zen
 */
public class SambaResponse {

    private String orig;
    private String base;
    private byte[] content;
    private static final byte[] EMPTY_CONTENT = new byte[0];
    private int code;
    private DocMetadata headers = new DocMetadata();
    private final Samba file;
    private Configuration conf;
    private MimeUtil MIME;

    /**
     * Returns the response code.
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Returns the value of a named header.
     */
    public String getHeader(String name) {
        return this.headers.get(name);
    }

    public byte[] getContent() {
        return this.content;
    }

    public Content toContent() {
        return new Content(this.orig, this.base, (this.content != null ? this.content : SambaResponse.EMPTY_CONTENT), this.getHeader(Response.CONTENT_TYPE), this.headers, this.conf);
    }

    public SambaResponse(URL url, CrawlDatum datum, Samba file, Configuration conf) throws SambaException, IOException {
        this.orig = url.toString();
        this.base = url.toString();
        this.file = file;
        this.conf = conf;
        this.MIME = new MimeUtil(conf);

        if (!"smb".equals(url.getProtocol())) {
            throw new SambaException("Not a samba url: " + url);
        }

        if (Samba.LOG.isTraceEnabled()) {
            Samba.LOG.trace("Fetching samba file: " + url);
        }

        String path = (url.getPath().isEmpty()) ? "/" : url.getPath();
        try {
            // specify the encoding via the config later?
            path = java.net.URLDecoder.decode(path, "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            // Do nothing
        }

        try {
            this.content = null;

            SmbFile f = new SmbFile(url);
            if (!f.exists()) {
                this.code = 404; // http Not Found

                return;
            }
            if (!f.canRead()) {
                this.code = 401; // http Unauthorized

                return;
            }

            if (f.lastModified() <= datum.getModifiedTime()) {
                this.code = 304;
                this.headers.set("Last-Modified", HTTPDateFormat.toString(f.lastModified()));

                return;
            }

            if (f.isDirectory()) {
                this.getDirAsHttpResponse(f);
            }
            else if (f.isFile()) {
                this.getFileAsHttpResponse(f);
            }
            else {
                this.code = 500; // http Internal Server Error
            }
        }
        catch (IOException e) {
            throw e;
        }
    }

    private void getFileAsHttpResponse(SmbFile f) throws SambaException, IOException {
        // ignore file of size larger than
        // Integer.MAX_VALUE = 2^31-1 = 2147483647
        long size = f.length();
        if (size > Integer.MAX_VALUE) {
            throw new SambaException("Samba file is too large, size: " + (size / 1024) + "KB");
            // or we can do this?
            // this.code = 400; // http Bad request
            // return;
        }

        // capture content
        int len = (int) size;

        if (this.file.maxContentLength >= 0 && len > this.file.maxContentLength) {
            len = this.file.maxContentLength;
        }

        this.content = new byte[len];

        InputStream is = f.getInputStream();
        int offset = 0;
        int n = 0;

        while (offset < len && (n = is.read(this.content, offset, len - offset)) >= 0) {
            offset += n;
        }

        if (offset < len) { // keep whatever already have, but issue a warning
            Samba.LOG.warn("Not enough bytes read from samba file: " + f.getPath());
        }

        is.close();

        // set headers
        this.headers.set(Response.CONTENT_LENGTH, new Long(size).toString());
        this.headers.set(Response.LAST_MODIFIED, HTTPDateFormat.toString(f.lastModified()));
        String mimeType = this.MIME.getMimeType(f.getInputStream());
        this.headers.set(Response.CONTENT_TYPE, mimeType != null ? mimeType : "");

        // response code
        this.code = 200; // http OK
    }

    private void getDirAsHttpResponse(SmbFile f) throws IOException {
        String path = f.toString();

        this.content = list2html(f.listFiles(), path);

        // set headers
        this.headers.set(Response.CONTENT_LENGTH, new Integer(this.content.length).toString());
        this.headers.set(Response.CONTENT_TYPE, "text/html");
        this.headers.set(Response.LAST_MODIFIED, HTTPDateFormat.toString(f.lastModified()));

        // response code
        this.code = 200; // http OK
    }

    private byte[] list2html(SmbFile[] list, String path) {
        StringBuilder doc = new StringBuilder();
        doc.append("<html>");
        doc.append("<head>");
        doc.append("<title>Index of ").append(path).append("</title>");
        doc.append("</head>");
        doc.append("<body>");
        doc.append("<h1>Index of ").append(path).append("</h1>");

        try {
            SmbFile f;
            for (int i = 0; i < list.length; i++) {
                f = list[i];
                String name = f.getName().replaceAll("\\s+", "+");
                String time = HTTPDateFormat.toString(f.lastModified());
                if (f.isDirectory()) {
                    doc.append("<a href=\"").append(this.base).append("/").append(name).append("\">").append(name).append("</a> ")
                            .append(time).append(" ");
                }
                else if (f.isFile()) {
                    doc.append("<a href=\"").append(this.base).append("/").append(name).append("\">").append(name).append("</a> ")
                            .append(time).append(" ")
                            .append(f.length()).append(" bytes").append(" ");
                }
            }
        }
        catch (SmbException e) {
            Samba.LOG.error("Error listing samba filesystem as HTML response. Error: " + e.toString(), e);
        }

        doc.append("</body>");
        doc.append("</html>");

        return new String(doc).getBytes();
    }
}
