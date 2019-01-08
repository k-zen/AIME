package io.aime.indexer;

// AIME
import io.aime.metadata.DocMetadata;

// Apache Hadoop
import org.apache.hadoop.io.VersionMismatchException;
import org.apache.hadoop.io.Writable;

// IO
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

// Util
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An AIME document contains information about a document that will be indexed.
 * i.e. Content Field -> Text of the document
 * <p>Also the weight here is important, because it's used to calculate the
 * score for the document. The weight can't be 0.0f or it'll break the scoring
 * system.</p>
 *
 * @author K-Zen
 */
public class AIMEDocument implements Writable, Iterable<Entry<String, AIMEField>> {

    public static final byte VERSION = 2;
    private Map<String, AIMEField> fields = new HashMap<String, AIMEField>();
    private DocMetadata documentMeta = new DocMetadata();
    private float weight = 1.0F;

    public void add(String name, String value) {
        fields.put(name, new AIMEField(value));
    }

    public String getFieldValue(String name) {
        AIMEField field = fields.get(name);
        if (field == null) {
            return "";
        }

        return field.getValue();
    }

    public AIMEField getField(String name) {
        return fields.get(name);
    }

    public AIMEField removeField(String name) {
        return fields.remove(name);
    }

    public Collection<String> getFieldNames() {
        return fields.keySet();
    }

    @Override
    public Iterator<Entry<String, AIMEField>> iterator() {
        return fields.entrySet().iterator();
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public DocMetadata getDocumentMeta() {
        return documentMeta;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        fields.clear(); // Clear all fields.

        // 1. Version (Byte)
        // 2. Weight (Float)
        // 3. DocumentMeta (Object)
        // 4. Field Size (Int) 
        // 5. Fields
        //    5.1. Name of Field (String)
        //    5.2. AIMEField (Object)
        byte version = in.readByte();
        if (version != VERSION) {
            throw new VersionMismatchException(VERSION, version);
        }
        weight = in.readFloat();
        documentMeta.readFields(in);
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String name = in.readUTF();
            AIMEField field = new AIMEField();
            field.readFields(in);
            fields.put(name, field);
        }
    }

    @Override
    public void write(DataOutput out) throws IOException {
        // 1. Version (Byte)
        // 2. Weight (Float)
        // 3. DocumentMeta (Object)
        // 4. Field Size (Int)
        // 5. Fields
        //    5.1. Name of Field (String)
        //    5.2. AIMEField (Object)
        out.writeByte(VERSION);
        out.writeFloat(weight);
        documentMeta.write(out);
        out.writeInt(fields.size());
        for (Map.Entry<String, AIMEField> entry : fields.entrySet()) {
            out.writeUTF(entry.getKey());
            entry.getValue().write(out);
        }
    }
}
