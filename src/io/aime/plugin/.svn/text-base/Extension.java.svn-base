package io.aime.plugin;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configurable;

// Util
import java.util.HashMap;

/**
 * An "Extension" is a kind of listener descriptor that will be installed on a
 * concrete ExtensionPoint that acts as kind of Publisher.
 *
 * @author K-Zen
 */
public final class Extension {

    private PluginDescriptor fDescriptor;
    private String fId;
    private String fTargetPoint;
    private String fClazz;
    private HashMap<String, String> fAttributes;
    private Configuration conf;
    private PluginRepository pluginRepository;

    public Extension(PluginDescriptor pDescriptor, String pExtensionPoint, String pId, String pExtensionClass, Configuration conf, PluginRepository pluginRepository) {
        this.fAttributes = new HashMap<String, String>();
        this.conf = conf;
        this.pluginRepository = pluginRepository;
        this.setDescriptor(pDescriptor);
        this.setExtensionPoint(pExtensionPoint);
        this.setId(pId);
        this.setClazz(pExtensionClass);
    }

    private void setExtensionPoint(String point) {
        this.fTargetPoint = point;
    }

    /**
     * Returns a attribute value, that is setuped in the manifest file and is
     * definied by the extension point xml schema.
     *
     * @param pKey - a key
     *
     * @return String a value
     */
    public String getAttribute(String pKey) {
        return this.fAttributes.get(pKey);
    }

    /**
     * Returns the full class name of the extension point implementation
     *
     * @return String
     */
    public String getClazz() {
        return this.fClazz;
    }

    /**
     * Return the unique id of the extension.
     *
     * @return String
     */
    public String getId() {
        return this.fId;
    }

    /**
     * Adds a attribute and is only used until model creation at plugin system
     * start up.
     *
     * @param pKey   a key
     * @param pValue a value
     */
    public void addAttribute(String pKey, String pValue) {
        this.fAttributes.put(pKey, pValue);
    }

    /**
     * Sets the Class that implement the concret extension and is only used
     * until model creation at system start up.
     *
     * @param extensionClazz The extensionClasname to set
     */
    public void setClazz(String extensionClazz) {
        this.fClazz = extensionClazz;
    }

    /**
     * Sets the unique extension Id and is only used until model creation at
     * system start up.
     *
     * @param extensionID The extensionID to set
     */
    public void setId(String extensionID) {
        this.fId = extensionID;
    }

    /**
     * Returns the Id of the extension point, that is implemented by this
     * extension.
     *
     * @return
     */
    public String getTargetPoint() {
        return this.fTargetPoint;
    }

    /**
     * Return an instance of the extension implementatio. Before we create a
     * extension instance we startup the plugin if it is not already done. The
     * plugin instance and the extension instance use the same
     * <code>PluginClassLoader</code>. Each Plugin use its own classloader. The
     * PluginClassLoader knows only own <i>Plugin runtime libraries </i> setuped
     * in the plugin manifest file and exported libraries of the depenedend
     * plugins.
     *
     * @return Object An instance of the extension implementation
     *
     * @throws PluginRuntimeException
     */
    public Object getExtensionInstance() throws PluginRuntimeException {
        // Must synchronize here to make sure creation and initialization
        // of a plugin instance and it extension instance are done by
        // one and only one thread.
        // The same is in PluginRepository.getPluginInstance().
        // Suggested by Stefan Groschupf <sg@media-style.com>
        synchronized (getId()) {
            try {
                PluginClassLoader loader = this.fDescriptor.getClassLoader();
                Class extensionClazz = loader.loadClass(getClazz());
                // lazy loading of Plugin in case there is no instance of the plugin
                // already.
                this.pluginRepository.getPluginInstance(getDescriptor());
                Object object = extensionClazz.newInstance();

                if (object instanceof Configurable) {
                    ((Configurable) object).setConf(this.conf);
                }

                return object;
            }
            catch (ClassNotFoundException e) {
                throw new PluginRuntimeException(e);
            }
            catch (InstantiationException e) {
                throw new PluginRuntimeException(e);
            }
            catch (IllegalAccessException e) {
                throw new PluginRuntimeException(e);
            }
        }
    }

    /**
     * return the plugin descriptor.
     *
     * @return PluginDescriptor
     */
    public PluginDescriptor getDescriptor() {
        return this.fDescriptor;
    }

    /**
     * Sets the plugin descriptor and is only used until model creation at
     * system start up.
     *
     * @param pDescriptor
     */
    public void setDescriptor(PluginDescriptor pDescriptor) {
        this.fDescriptor = pDescriptor;
    }
}
