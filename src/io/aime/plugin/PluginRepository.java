package io.aime.plugin;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

// Lang
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

// Log4j
import org.apache.log4j.Logger;

// Util
import java.util.ArrayList;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The plugin repositority is a registry of all plugins. At system boot up a
 * repositority is builded by parsing the mainifest files of all plugins.
 * Plugins that require not existing other plugins are not registed. For each
 * plugin a plugin descriptor instance will be created. The descriptor
 * represents all meta information about a plugin. So a plugin instance will be
 * created later when it is required, this allow lazy plugin loading.
 */
public class PluginRepository {

    private static final WeakHashMap<Configuration, PluginRepository> CACHE = new WeakHashMap<Configuration, PluginRepository>();
    private boolean auto;
    private List<PluginDescriptor> fRegisteredPlugins;
    private HashMap<String, ExtensionPoint> fExtensionPoints;
    private HashMap<String, Plugin> fActivatedPlugins;
    private Configuration conf;
    private static final String KEY = PluginRepository.class.getName();
    private static final Logger LOG = Logger.getLogger(KEY);

    public PluginRepository(Configuration conf) throws RuntimeException {
        fActivatedPlugins = new HashMap<String, Plugin>();
        fExtensionPoints = new HashMap<String, ExtensionPoint>();
        this.conf = conf;
        this.auto = conf.getBoolean("plugin.auto-activation", true);
        String[] pluginFolders = conf.getStrings("plugin.folders");
        PluginManifestParser manifestParser = new PluginManifestParser(conf, this);
        Map<String, PluginDescriptor> allPlugins = manifestParser.parsePluginFolder(pluginFolders);
        Pattern excludes = Pattern.compile(conf.get("plugin.excludes", ""));
        Pattern includes = Pattern.compile(conf.get("plugin.includes", ""));
        Map<String, PluginDescriptor> filteredPlugins = this.filter(excludes, includes, allPlugins);

        // Chequeo de dependencias.
        fRegisteredPlugins = this.getDependencyCheckedPlugins(filteredPlugins, this.auto ? allPlugins : filteredPlugins);

        this.installExtensionPoints(fRegisteredPlugins);

        try {
            this.installExtensions(fRegisteredPlugins);
        }
        catch (PluginRuntimeException e) {
            LOG.fatal(e.toString());

            throw new RuntimeException(e.getMessage());
        }

        // this.displayStatus ();
    }

    public static synchronized PluginRepository get(Configuration conf) {
        PluginRepository result = CACHE.get(conf);
        if (result == null) {
            result = new PluginRepository(conf);
            CACHE.put(conf, result);
        }

        return result;
    }

    private void installExtensionPoints(List<PluginDescriptor> plugins) {
        if (plugins == null) {
            return;
        }

        for (PluginDescriptor plugin : plugins) {
            for (ExtensionPoint point : plugin.getExtenstionPoints()) {
                String xpId = point.getId();

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Anadiendo punto de extension -> " + xpId);
                }

                fExtensionPoints.put(xpId, point);
            }
        }
    }

    private void installExtensions(List<PluginDescriptor> pRegisteredPlugins) throws PluginRuntimeException {
        for (PluginDescriptor descriptor : pRegisteredPlugins) {
            for (Extension extension : descriptor.getExtensions()) {
                String xpId = extension.getTargetPoint();
                ExtensionPoint point = this.getExtensionPoint(xpId);

                if (point == null) {
                    throw new PluginRuntimeException("Plugin -> [" + descriptor.getPluginId() + "], punto de extension apunta a -> " + xpId + ", y este no existe.");
                }

                point.addExtension(extension);
            }
        }
    }

    private void getPluginCheckedDependencies(PluginDescriptor plugin, Map<String, PluginDescriptor> plugins, Map<String, PluginDescriptor> dependencies, Map<String, PluginDescriptor> branch)
            throws MissingDependencyException, CircularDependencyException {
        if (dependencies == null) {
            dependencies = new HashMap<String, PluginDescriptor>();
        }

        if (branch == null) {
            branch = new HashMap<String, PluginDescriptor>();
        }

        branch.put(plugin.getPluginId(), plugin);

        // Otherwise, checks each dependency
        for (String id : plugin.getDependencies()) {
            PluginDescriptor dependency = plugins.get(id);

            if (dependency == null) {
                throw new MissingDependencyException("Dependencia faltante -> " + id + " para Plugin -> " + plugin.getPluginId());
            }

            if (branch.containsKey(id)) {
                throw new CircularDependencyException("Dependencia circular detectada -> " + id + " para Plugin -> " + plugin.getPluginId());
            }

            dependencies.put(id, dependency);
            this.getPluginCheckedDependencies(plugins.get(id), plugins, dependencies, branch);
        }

        branch.remove(plugin.getPluginId());
    }

    private Map<String, PluginDescriptor> getPluginCheckedDependencies(PluginDescriptor plugin, Map<String, PluginDescriptor> plugins)
            throws MissingDependencyException, CircularDependencyException {
        Map<String, PluginDescriptor> dependencies = new HashMap<String, PluginDescriptor>();
        Map<String, PluginDescriptor> branch = new HashMap<String, PluginDescriptor>();

        this.getPluginCheckedDependencies(plugin, plugins, dependencies, branch);

        return dependencies;
    }

    private List<PluginDescriptor> getDependencyCheckedPlugins(Map<String, PluginDescriptor> filtered, Map<String, PluginDescriptor> all) {
        if (filtered == null) {
            return null;
        }

        Map<String, PluginDescriptor> checked = new HashMap<String, PluginDescriptor>();

        for (PluginDescriptor plugin : filtered.values()) {
            try {
                checked.putAll(this.getPluginCheckedDependencies(plugin, all));
                checked.put(plugin.getPluginId(), plugin);
            }
            catch (MissingDependencyException mde) {
                // Log exception and ignore plugin
                LOG.warn(mde.getMessage());
            }
            catch (CircularDependencyException cde) {
                // Simply ignore this plugin
                LOG.warn(cde.getMessage());
            }
        }

        return new ArrayList<PluginDescriptor>(checked.values());
    }

    /**
     * Returns all registed plugin descriptors.
     *
     * @return PluginDescriptor[]
     */
    public PluginDescriptor[] getPluginDescriptors() {
        return fRegisteredPlugins.toArray(new PluginDescriptor[fRegisteredPlugins.size()]);
    }

    /**
     * Returns the descriptor of one plugin identified by a plugin id.
     *
     * @param pPluginId
     *
     * @return PluginDescriptor
     */
    public PluginDescriptor getPluginDescriptor(String pPluginId) {
        for (PluginDescriptor descriptor : fRegisteredPlugins) {
            if (descriptor.getPluginId().equals(pPluginId)) {
                return descriptor;
            }
        }

        return null;
    }

    /**
     * Returns a extension point indentified by a extension point id.
     *
     * @param pXpId
     *
     * @return a extentsion point
     */
    public ExtensionPoint getExtensionPoint(String pXpId) {
        return this.fExtensionPoints.get(pXpId);
    }

    /**
     * Returns a instance of a plugin. Plugin instances are cached. So a plugin
     * exist only as one instance. This allow a central management of plugin own
     * resources. After creating the plugin instance the startUp() method is
     * invoked. The plugin use a own classloader that is used as well by all
     * instance of extensions of the same plugin. This class loader use all
     * exported libraries from the dependend plugins and all plugin libraries.
     *
     * @param pDescriptor
     *
     * @return Plugin
     *
     * @throws PluginRuntimeException
     */
    public Plugin getPluginInstance(PluginDescriptor pDescriptor) throws PluginRuntimeException {
        if (fActivatedPlugins.containsKey(pDescriptor.getPluginId())) {
            return fActivatedPlugins.get(pDescriptor.getPluginId());
        }

        try {
            // Must synchronize here to make sure creation and initialization
            // of a plugin instance are done by one and only one thread.
            // The same is in Extension.getExtensionInstance().
            // Suggested by Stefan Groschupf <sg@media-style.com>
            synchronized (pDescriptor) {
                PluginClassLoader loader = pDescriptor.getClassLoader();
                Class pluginClass = loader.loadClass(pDescriptor.getPluginClass());
                @SuppressWarnings("unchecked")
                Constructor constructor = pluginClass.getConstructor(new Class[]{PluginDescriptor.class, Configuration.class});
                Plugin plugin = (Plugin) constructor.newInstance(new Object[]{pDescriptor, this.conf});
                plugin.startUp();
                fActivatedPlugins.put(pDescriptor.getPluginId(), plugin);

                return plugin;
            }
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
        catch (NoSuchMethodException e) {
            throw new PluginRuntimeException(e);
        }
        catch (InvocationTargetException e) {
            throw new PluginRuntimeException(e);
        }
    }

    @Override
    public void finalize() throws Throwable {
        this.shotDownActivatedPlugins();
    }

    /**
     * Shuts down all plugins.
     *
     * @throws PluginRuntimeException
     */
    private void shotDownActivatedPlugins() throws PluginRuntimeException {
        for (Plugin plugin : fActivatedPlugins.values()) {
            plugin.shutDown();
        }
    }

    private void displayStatus() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Modo Auto-Activacion de Plugins -> " + this.auto);
            LOG.info("Plugins Registrados:");
        }

        if ((fRegisteredPlugins == null) || (fRegisteredPlugins.size() == 0) && LOG.isInfoEnabled()) {
            LOG.info("\tNinguno");
        }
        else {
            for (PluginDescriptor plugin : fRegisteredPlugins) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("\t" + plugin.getName() + " -> [" + plugin.getPluginId() + "]");
                }
            }
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Puntos de Extension Registrados:");
        }

        if ((fExtensionPoints == null) || (fExtensionPoints.size() == 0) && LOG.isInfoEnabled()) {
            LOG.info("\tNinguno");
        }
        else {
            for (ExtensionPoint ep : fExtensionPoints.values()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("\t" + ep.getName() + " -> [" + ep.getId() + "]");
                }
            }
        }
    }

    /**
     * Filters a list of plugins. The list of plugins is filtered regarding the
     * configuration properties
     * <code>plugin.excludes</code> and
     * <code>plugin.includes</code>.
     *
     * @param excludes
     * @param includes
     * @param plugins  Map of plugins
     *
     * @return map of plugins matching the configuration
     */
    private Map<String, PluginDescriptor> filter(Pattern excludes, Pattern includes, Map<String, PluginDescriptor> plugins) {
        Map<String, PluginDescriptor> map = new HashMap<String, PluginDescriptor>();

        if (plugins == null) {
            return map;
        }

        for (PluginDescriptor plugin : plugins.values()) {
            if (plugin == null) {
                continue;
            }

            String id = plugin.getPluginId();

            if (id == null) {
                continue;
            }

            if (!includes.matcher(id).matches()) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("No incluyendo -> " + id);
                }

                continue;
            }
            if (excludes.matcher(id).matches()) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Excluyendo -> " + id);
                }

                continue;
            }
            map.put(plugin.getPluginId(), plugin);
        }

        return map;
    }
}
