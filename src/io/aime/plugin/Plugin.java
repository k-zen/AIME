package io.aime.plugin;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

/**
 * A AIME-plugin is an container for a set of custom logic that provide
 * extensions to the AIME core functionality or another plugin that provides an
 * API for extending. A plugin can provide one or a set of extensions.
 * Extensions are components that can be dynamically installed as a kind of
 * listener to extension points. Extension points are a kind of publisher that
 * provide a API and invoke one or a set of installed extensions. Each plugin
 * may extend the base
 * <code>Plugin</code>.
 * <code>Plugin</code> instances are used as the point of life cycle managemet
 * of plugin related functionality. The
 * <code>Plugin</code> will be startuped and shutdown by the AIME plugin
 * management system. A possible usecase of the
 * <code>Plugin</code> implementation is to create or close a database
 * connection.
 * @author joa23
 */
public class Plugin {

    private PluginDescriptor fDescriptor;
    protected Configuration conf;

    public Plugin(PluginDescriptor pDescriptor, Configuration conf) {
        setDescriptor(pDescriptor);
        this.conf = conf;
    }

    /**
     * Will be invoked until plugin start up. Since the AIME-plugin system use
     * lazy loading the start up is invoked until the first time a extension is
     * used.
     * @throws PluginRuntimeException - If the startup was without successs.
     */
    public void startUp() throws PluginRuntimeException {
    }

    /**
     * Shutdown the plugin. This happens until AIME will be stopped.
     * @throws PluginRuntimeException - if a problems occurs until shutdown the
     *                                plugin.
     */
    public void shutDown() throws PluginRuntimeException {
    }

    /**
     * Returns the plugin descriptor
     * @return PluginDescriptor
     */
    public PluginDescriptor getDescriptor() {
        return fDescriptor;
    }

    private void setDescriptor(PluginDescriptor descriptor) {
        fDescriptor = descriptor;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        shutDown();
    }
}
