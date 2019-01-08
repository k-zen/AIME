package io.aime.crawl;

import io.aime.util.ObjectCache;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

/**
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class FetchScheduleFactory
{

    private static final Logger LOG = Logger.getLogger(FetchScheduleFactory.class.getName());

    public static FetchSchedule getFetchSchedule(Configuration conf)
    {
        String clazz = conf.get("db.fetch.schedule.class", DefaultFetchSchedule.class.getName());
        ObjectCache objectCache = ObjectCache.get(conf);
        FetchSchedule impl = (FetchSchedule) objectCache.getObject(clazz);

        if (impl == null)
        {
            try
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Using implementation of FetchSchedule: " + clazz);
                }

                Class<?> implClass = Class.forName(clazz);
                impl = (FetchSchedule) implClass.newInstance();
                impl.setConf(conf);
                objectCache.setObject(clazz, impl);
            }
            catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
            {
                throw new RuntimeException("Can't implement class: " + clazz, e);
            }
        }

        return impl;
    }
}
