package io.aime.protocol;

// AIME
import io.aime.metadata.DocMetadata;
import io.aime.util.AIMEConfiguration;
import io.aime.util.MimeUtil;

// Apache Hadoop
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.UTF8;
import org.apache.hadoop.io.VersionMismatchException;
import org.apache.hadoop.io.Writable;

// IO
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;

// Log4j
import org.apache.log4j.Logger;

// Util
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

/**
 * This class represents the Contents of an entry in a given segment.
 *
 * @author Nutch.org
 * @author K-Zen
 */
public final class Content implements Writable, Configurable {

    private static final Logger LOG = Logger.getLogger(Content.class.getName());
    private final static int VERSION = -1;
    public static final String DIR_NAME = "content";
    private Configuration conf;
    private int version = Content.VERSION;
    private String url = new String();
    private String base = new String();
    private byte[] content = new byte[0];
    private String contentType = new String();
    private DocMetadata metadata = new DocMetadata();
    private MimeUtil mimeTypes = new MimeUtil();

    @Override
    public final void setConf(Configuration conf) {
        if (conf == null) {
            this.conf = new AIMEConfiguration().create();
        }
        else {
            this.conf = conf;
        }
    }

    @Override
    public final Configuration getConf() {
        if (this.conf == null) {
            this.setConf(null);
        }

        return this.conf;
    }

    public Content() {
    }

    public Content(String url, String base, byte[] content, String contentType, DocMetadata metadata, Configuration conf) {
        if (url == null) {
            throw new IllegalArgumentException("Null URL.");
        }
        if (base == null) {
            throw new IllegalArgumentException("null Base.");
        }
        if (content == null) {
            throw new IllegalArgumentException("Null Content.");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("Null Metadata.");
        }

        this.url = url;
        this.base = base;
        this.content = content;
        this.metadata = metadata;
        this.mimeTypes = new MimeUtil(conf);
        this.contentType = this.getContentType(contentType, url, content);
    }

    private final void readFieldsCompressed(DataInput in) throws IOException {
        byte oldVersion = in.readByte();
        switch (oldVersion) {
            case 0:
            case 1:
                url = UTF8.readString(in);
                base = UTF8.readString(in);
                content = new byte[in.readInt()];
                in.readFully(content);
                contentType = UTF8.readString(in);

                /*
                 * Reconstruct Metadata.
                 */
                int keySize = in.readInt();
                String key;
                for (int i = 0; i < keySize; i++) {
                    key = UTF8.readString(in);
                    int valueSize = in.readInt();
                    for (int j = 0; j < valueSize; j++) {
                        metadata.add(key, UTF8.readString(in));
                    }
                }
                break;
            case 2:
                url = Text.readString(in);
                base = Text.readString(in);
                content = new byte[in.readInt()];
                in.readFully(content);
                contentType = Text.readString(in);
                metadata.readFields(in);
                break;
            default:
                throw new VersionMismatchException((byte) 2, oldVersion);
        }
    }

    @Override
    public final void readFields(DataInput in) throws IOException {
        metadata.clear();

        int sizeOrVersion = in.readInt();
        if (sizeOrVersion < 0) {
            version = sizeOrVersion;
            switch (version) {
                case VERSION:
                    url = Text.readString(in);
                    base = Text.readString(in);
                    content = new byte[in.readInt()];
                    in.readFully(content);
                    contentType = Text.readString(in);
                    metadata.readFields(in);
                    break;
                default:
                    throw new VersionMismatchException((byte) VERSION, (byte) version);
            }
        }
        else {
            byte[] compressed = new byte[sizeOrVersion];
            in.readFully(compressed, 0, compressed.length);
            ByteArrayInputStream deflated = new ByteArrayInputStream(compressed);
            DataInput inflater = new DataInputStream(new InflaterInputStream(deflated));
            this.readFieldsCompressed(inflater);
        }
    }

    @Override
    public final void write(DataOutput out) throws IOException {
        out.writeInt(VERSION);
        Text.writeString(out, url);
        Text.writeString(out, base);
        out.writeInt(content.length);
        out.write(content);
        Text.writeString(out, contentType);
        metadata.write(out);
    }

    public static Content read(DataInput in) throws IOException {
        Content content = new Content();
        content.readFields(in);

        return content;
    }

    public String getUrl() {
        return this.url;
    }

    /**
     * The base url for relative links contained in the content. Maybe be
     * different from url if the request redirected.
     *
     * @return The base URL.
     */
    public String getBaseUrl() {
        return this.base;
    }

    /**
     * The binary content retrieved.
     *
     * @return A byte array with the contents of the entry.
     */
    public byte[] getContent() {
        return this.content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * The media type of the retrieved content.
     *
     * @return The content media type.
     *
     * @see <a
     * href="http://www.iana.org/assignments/media-types/">http://www.iana.org/assignments/media-types/</a>
     */
    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Other protocol-specific data.
     *
     * @return The metadata associated with the entry.
     */
    public DocMetadata getMetadata() {
        return this.metadata;
    }

    /**
     * Other protocol-specific data.
     *
     * @param metadata The metadata associated with the entry.
     */
    public void setMetadata(DocMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Content)) {
            return false;
        }

        Content that = (Content) o;

        return this.url.equals(that.url) && this.base.equals(that.base) && Arrays.equals(this.getContent(), that.getContent()) && this.contentType.equals(that.contentType) && this.metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + this.version;
        hash = 61 * hash + (this.url != null ? this.url.hashCode() : 0);
        hash = 61 * hash + (this.base != null ? this.base.hashCode() : 0);
        hash = 61 * hash + Arrays.hashCode(this.content);
        hash = 61 * hash + (this.contentType != null ? this.contentType.hashCode() : 0);
        hash = 61 * hash + (this.metadata != null ? this.metadata.hashCode() : 0);
        hash = 61 * hash + (this.mimeTypes != null ? this.mimeTypes.hashCode() : 0);

        return hash;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("Version: " + this.version + "\n");
        buffer.append("url: " + this.url + "\n");
        buffer.append("base: " + this.base + "\n");
        buffer.append("contentType: " + this.contentType + "\n");
        buffer.append("metadata: " + this.metadata + "\n");
        buffer.append("Content:\n");
        if (this.content != null && this.content.length > 0) {
            buffer.append(new String(this.content)); // try default encoding
        }

        return buffer.toString();

    }

    private String getContentType(String typeName, String url, byte[] data) {
        return this.mimeTypes.autoResolveContentType(typeName, url, data);
    }
}
