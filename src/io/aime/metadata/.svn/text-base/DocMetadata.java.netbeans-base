package io.aime.metadata;

// Apache Hadoop
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

// IO
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

// Util
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A multi-valued metadata container.
 *
 * @author Chris Mattmann
 * @author Jerome Charron
 * @author K-Zen
 */
public class DocMetadata implements Writable, CreativeCommons, DublinCore, HTTPHeaders, Office, Feed {

    private Map<String, String[]> metadata = new HashMap<String, String[]>();

    public DocMetadata() {
    }

    /**
     * Returns true if named value is multivalued.
     *
     * @param name name of metadata
     *
     * @return true is named value is multivalued, false if single value or null
     */
    public boolean isMultivalued(final String name) {
        return this.metadata.get(name) != null && this.metadata.get(name).length > 1;
    }

    /**
     * Returns an array of the names contained in the metadata.
     *
     * @return Metadata names
     */
    public String[] names() {
        return this.metadata.keySet().toArray(new String[this.metadata.keySet().size()]);
    }

    /**
     * Get the value associated to a metadata name.
     *
     * <p>If many values are assiociated to the specified name, then the first
     * one is returned.</p>
     *
     * @param name of the metadata.
     *
     * @return the value associated to the specified metadata name.
     */
    public String get(final String name) {
        String[] values = this.metadata.get(name);

        if (values == null) {
            return null;
        }
        else {
            return values[0];
        }
    }

    public String[] getValues(final String name) {
        return this._getValues(name);
    }

    private String[] _getValues(final String name) {
        String[] values = this.metadata.get(name);

        if (values == null) {
            values = new String[0];
        }

        return values;
    }

    /**
     * Add a metadata name/value mapping. Add the specified value to the list of
     * values associated to the specified metadata name.
     *
     * @param name  the metadata name.
     * @param value the metadata value.
     */
    public void add(final String name, final String value) {
        String[] values = this.metadata.get(name);

        if (values == null) {
            this.set(name, value);
        }
        else {
            String[] newValues = new String[values.length + 1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[newValues.length - 1] = value;
            this.metadata.put(name, newValues);
        }
    }

    /**
     * Copy All key-value pairs from properties.
     *
     * @param properties properties to copy from
     */
    public void setAll(Properties properties) {
        Enumeration names = properties.propertyNames();

        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            this.metadata.put(name, new String[]{properties.getProperty(name)});
        }
    }

    /**
     * Set metadata name/value. Associate the specified value to the specified
     * metadata name. If some previous values were associated to this name, they
     * are removed.
     *
     * @param name  the metadata name.
     * @param value the metadata value.
     */
    public void set(String name, String value) {
        this.metadata.put(name, new String[]{value});
    }

    /**
     * Remove a metadata and all its associated values.
     *
     * @param name metadata name to remove
     */
    public void remove(String name) {
        this.metadata.remove(name);
    }

    /**
     * Returns the number of metadata names in this metadata.
     *
     * @return number of metadata names
     */
    public int size() {
        return this.metadata.size();
    }

    /**
     * Remove all mappings from metadata.
     */
    public void clear() {
        this.metadata.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        DocMetadata other = null;
        try {
            other = (DocMetadata) o;
        }
        catch (ClassCastException cce) {
            return false;
        }

        if (other.size() != size()) {
            return false;
        }

        String[] names = names();
        for (int i = 0; i < names.length; i++) {
            String[] otherValues = other._getValues(names[i]);
            String[] thisValues = this._getValues(names[i]);

            if (otherValues.length != thisValues.length) {
                return false;
            }
            for (int j = 0; j < otherValues.length; j++) {
                if (!otherValues[j].equals(thisValues[j])) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + (this.metadata == null ? 0 : this.metadata.hashCode());

        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        String[] names = names();

        for (int i = 0; i < names.length; i++) {
            String[] values = this._getValues(names[i]);
            for (int j = 0; j < values.length; j++) {
                buf.append(names[i]).append("=").append(values[j]).append(" ");
            }
        }

        return buf.toString();
    }

    @Override
    public final void write(DataOutput out) throws IOException {
        out.writeInt(size());
        String[] values = null;
        String[] names = names();

        for (int i = 0; i < names.length; i++) {
            Text.writeString(out, names[i]);
            values = this._getValues(names[i]);
            int cnt = 0;

            for (int j = 0; j < values.length; j++) {
                if (values[j] != null) {
                    cnt++;
                }
            }

            out.writeInt(cnt);
            for (int j = 0; j < values.length; j++) {
                if (values[j] != null) {
                    Text.writeString(out, values[j]);
                }
            }
        }
    }

    @Override
    public final void readFields(DataInput in) throws IOException {
        int keySize = in.readInt();
        String key;

        for (int i = 0; i < keySize; i++) {
            key = Text.readString(in);
            int valueSize = in.readInt();

            for (int j = 0; j < valueSize; j++) {
                this.add(key, Text.readString(in));
            }
        }
    }
}
