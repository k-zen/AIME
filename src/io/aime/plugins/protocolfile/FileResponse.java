package io.aime.plugins.protocolfile;

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
import java.io.UnsupportedEncodingException;

// Net
import java.net.URI;

/**
 * FileResponse.java mimics file replies as http response.
 *
 * <p>It tries its best to follow http's way for headers, response codes as well
 * as exceptions.</p>
 *
 * <p>Comments: (1) java.net.URL and java.net.URLConnection can handle file:
 * scheme. However they are not flexible enough, so not used in this
 * implementation.</p>
 *
 * <p>(2) java.io.File is used for its abstractness across platforms. Warning:
 * java.io.File API (1.4.2) does not elaborate on how special files, such as
 * /dev/* in unix and /proc/* on linux, are treated. Tests show (a)
 * java.io.File.isFile() return false for /dev/* (b) java.io.File.isFile()
 * return true for /proc/* (c) java.io.File.length() return 0 for /proc/* We are
 * probably oaky for now. Could be buggy here. How about special files on
 * windows?</p>
 *
 * <p>(3) java.io.File API (1.4.2) does not seem to know unix hard link files.
 * They are just treated as individual files.</p>
 *
 * <p>(4) No funcy POSIX file attributes yet. May never need?</p>
 *
 * @author John Xing
 * @author K-Zen
 */
public class FileResponse {

    private String orig;
    private String base;
    private byte[] content;
    private static final byte[] EMPTY_CONTENT = new byte[0];
    private int code;
    private DocMetadata headers = new DocMetadata();
    private final File file;
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
        return new Content(this.orig, this.base, (this.content != null ? this.content : FileResponse.EMPTY_CONTENT), this.getHeader(Response.CONTENT_TYPE), this.headers, this.conf);
    }

    public FileResponse(URI uri, CrawlDatum datum, File file, Configuration conf) throws FileException, IOException {
        this.orig = uri.toString();
        this.base = uri.toString();
        this.file = file;
        this.conf = conf;
        this.MIME = new MimeUtil(conf);

        if (!"file".equals(uri.getScheme())) {
            throw new FileException("Not a file url: " + uri);
        }

        if (File.LOG.isTraceEnabled()) {
            File.LOG.trace("Fetching file: " + uri);
        }

        String path = (uri.getPath().isEmpty()) ? "/" : uri.getPath();

        try {
            // specify the encoding via the config later?
            path = java.net.URLDecoder.decode(path, "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            // Do nothing
        }

        try {
            this.content = null;

            // url.toURI() is only in j2se 1.5.0
            //java.io.File f = new java.io.File(url.toURI());
            java.io.File f = new java.io.File(path);

            if (!f.exists()) {
                this.code = 404; // http Not Found

                return;
            }

            if (!f.canRead()) {
                this.code = 401; // http Unauthorized

                return;
            }

            // symbolic link or relative path on unix
            // fix me: what's the consequence on windows platform
            // where case is insensitive
            if (!f.equals(f.getCanonicalFile())) {
                // we want to automatically escape characters that are illegal in URLs. 
                // It is recommended that new code convert an abstract pathname into a URL 
                // by first converting it into a URI, via the toURI method, and then 
                // converting the URI into a URL via the URI.toURL method.
                this.headers.set(Response.LOCATION, f.getCanonicalFile().toURI().toURL().toString());
                this.code = 300; // http redirect

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

    private void getFileAsHttpResponse(java.io.File f) throws FileException, IOException {
        // ignore file of size larger than
        // Integer.MAX_VALUE = 2^31-1 = 2147483647
        long size = f.length();
        if (size > Integer.MAX_VALUE) {
            throw new FileException("File is too large, size: " + (size / 1024) + "KB");
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

        java.io.InputStream is = new java.io.FileInputStream(f);
        int offset = 0;
        int n = 0;

        while (offset < len && (n = is.read(this.content, offset, len - offset)) >= 0) {
            offset += n;
        }

        if (offset < len) { // keep whatever already have, but issue a warning
            File.LOG.warn("Not enough bytes read from file: " + f.getPath());
        }

        is.close();

        // set headers
        this.headers.set(Response.CONTENT_LENGTH, new Long(size).toString());
        this.headers.set(Response.LAST_MODIFIED, HTTPDateFormat.toString(f.lastModified()));
        String mimeType = this.MIME.getMimeType(f);
        this.headers.set(Response.CONTENT_TYPE, mimeType != null ? mimeType : "");

        // response code
        this.code = 200; // http OK
    }

    private void getDirAsHttpResponse(java.io.File f) throws IOException {
        String path = f.toString();

        this.content = list2html(f.listFiles(), path);

        // set headers
        this.headers.set(Response.CONTENT_LENGTH, new Integer(this.content.length).toString());
        this.headers.set(Response.CONTENT_TYPE, "text/html");
        this.headers.set(Response.LAST_MODIFIED, HTTPDateFormat.toString(f.lastModified()));

        // response code
        this.code = 200; // http OK
    }

    private byte[] list2html(java.io.File[] list, String path) {
        StringBuilder doc = new StringBuilder();
        doc.append("<html>");
        doc.append("<head>");
        doc.append("<title>Index of ").append(path).append("</title>");
        doc.append("</head>");
        doc.append("<body>");
        doc.append("<h1>Index of ").append(path).append("</h1>");

        java.io.File f;
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

        doc.append("</body>");
        doc.append("</html>");

        return new String(doc).getBytes();
    }
}
