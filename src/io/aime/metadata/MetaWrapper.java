package io.aime.metadata;

// AIME
import io.aime.crawl.AIMEWritable;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;

// IO
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * This is a simple decorator that adds metadata to any Writable-s that can be
 * serialized by <tt>AIMEWritable</tt>.
 *
 * <p>This is useful when data needs to be temporarily enriched during
 * processing, but this temporary metadata doesn't need to be permanently stored
 * after the job is done.</p>
 *
 * @author Andrzej Bialecki
 * @author K-Zen
 */
public class MetaWrapper extends AIMEWritable {

    private DocMetadata metadata;

    public MetaWrapper() {
        super();
        this.metadata = new DocMetadata();
    }

    public MetaWrapper(Writable instance, Configuration conf) {
        super(instance);
        this.metadata = new DocMetadata();
        setConf(conf);
    }

    public MetaWrapper(DocMetadata metadata, Writable instance, Configuration conf) {
        super(instance);
        if (metadata == null) {
            metadata = new DocMetadata();
        }
        this.metadata = metadata;
        setConf(conf);
    }

    /**
     * Get all metadata.
     *
     * @return
     */
    public DocMetadata getMetadata() {
        return this.metadata;
    }

    /**
     * Add metadata. See {@link Metadata#add(String, String)} for more
     * information.
     *
     * @param name  metadata name
     * @param value metadata value
     */
    public void addMeta(String name, String value) {
        this.metadata.add(name, value);
    }

    /**
     * Set metadata. See {@link Metadata#set(String, String)} for more
     * information.
     *
     * @param name
     * @param value
     */
    public void setMeta(String name, String value) {
        this.metadata.set(name, value);
    }

    /**
     * Get metadata. See {@link Metadata#get(String)} for more information.
     *
     * @param name
     *
     * @return metadata value
     */
    public String getMeta(String name) {
        return this.metadata.get(name);
    }

    /**
     * Get multiple metadata. See {@link Metadata#getValues(String)} for more
     * information.
     *
     * @param name
     *
     * @return multiple values
     */
    public String[] getMetaValues(String name) {
        return this.metadata.getValues(name);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        super.readFields(in);
        this.metadata = new DocMetadata();
        this.metadata.readFields(in);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        super.write(out);
        this.metadata.write(out);
    }
}
