package io.aime.util;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

// Log4j
import org.apache.log4j.Logger;

// Util
import java.util.HashMap;
import java.util.WeakHashMap;

public class ObjectCache {

    private static final Logger LOG = Logger.getLogger(ObjectCache.class.getName());
    private static final WeakHashMap<Configuration, ObjectCache> CACHE = new WeakHashMap<Configuration, ObjectCache>();
    private final HashMap<String, Object> objectMap;

    private ObjectCache() {
        this.objectMap = new HashMap<String, Object>();
    }

    public static ObjectCache get(Configuration conf) {
        ObjectCache objectCache = ObjectCache.CACHE.get(conf);

        if (objectCache == null) {
            objectCache = new ObjectCache();
            ObjectCache.CACHE.put(conf, objectCache);
        }

        return objectCache;
    }

    public Object getObject(String key) {
        return this.objectMap.get(key);
    }

    public void setObject(String key, Object value) {
        this.objectMap.put(key, value);
    }
}
