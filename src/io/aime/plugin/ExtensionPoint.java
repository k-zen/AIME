package io.aime.plugin;

// Util
import java.util.ArrayList;

/**
 * The
 * <code>ExtensionPoint</code> provide meta information of a extension point.
 *
 * @author K-Zen
 */
public class ExtensionPoint {

    private String ftId;
    private String fName;
    private String fSchema;
    private ArrayList<Extension> fExtensions;

    public ExtensionPoint(String pId, String pName, String pSchema) {
        setId(pId);
        setName(pName);
        setSchema(pSchema);
        fExtensions = new ArrayList<Extension>();
    }

    /**
     * Returns the unique id of the extension point.
     * @return String
     */
    public String getId() {
        return ftId;
    }

    /**
     * Returns the name of the extension point.
     * @return String
     */
    public String getName() {
        return fName;
    }

    /**
     * Returns a path to the xml schema of a extension point.
     * @return String
     */
    public String getSchema() {
        return fSchema;
    }

    /**
     * Sets the extensionPointId.
     * @param pId extension point id
     */
    private void setId(String pId) {
        ftId = pId;
    }

    /**
     * Sets the extension point name.
     * @param pName
     */
    private void setName(String pName) {
        fName = pName;
    }

    /**
     * Sets the schema.
     * @param pSchema
     */
    private void setSchema(String pSchema) {
        fSchema = pSchema;
    }

    /**
     * Install a coresponding extension to this extension point.
     * @param extension
     */
    public void addExtension(Extension extension) {
        fExtensions.add(extension);
    }

    /**
     * Returns a array of extensions that lsiten to this extension point.
     * @return Extension[]
     */
    public Extension[] getExtensions() {
        return fExtensions.toArray(new Extension[fExtensions.size()]);
    }
}