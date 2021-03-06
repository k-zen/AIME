package io.aime.crawl;

import io.aime.net.URLNormalizers;
import io.aime.util.URLUtil;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Partitioner;
import org.apache.log4j.Logger;

/**
 * Partition urls by host, domain name or IP depending on the value of the
 * parameter 'partition.url.mode' which can be 'byHost', 'byDomain' or 'byIP'.
 *
 * @author Apache Nutch
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class URLPartitioner implements Partitioner<Text, Writable>
{

    private static final String KEY = URLPartitioner.class.getName();
    private static final Logger LOG = Logger.getLogger(KEY);
    public static final String PARTITION_MODE_KEY = "partition.url.mode";
    public static final String PARTITION_MODE_HOST = "byHost";
    public static final String PARTITION_MODE_DOMAIN = "byDomain";
    public static final String PARTITION_MODE_IP = "byIP";
    private int seed;
    private URLNormalizers normalizers;
    private String mode = PARTITION_MODE_HOST;

    @Override
    public void configure(JobConf job)
    {
        seed = job.getInt("partition.url.seed", 0);
        mode = job.get(PARTITION_MODE_KEY, PARTITION_MODE_HOST);

        // check that the mode is known
        if (!mode.equals(PARTITION_MODE_IP) && !mode.equals(PARTITION_MODE_DOMAIN) && !mode.equals(PARTITION_MODE_HOST))
        {
            LOG.error("Irreconocible modo de particion -> " + mode + ", forzando a [byHost].");
            mode = PARTITION_MODE_HOST;
        }

        normalizers = new URLNormalizers(job, URLNormalizers.SCOPE_PARTITION);
    }

    public void close()
    {
    }

    @Override
    public int getPartition(Text key, Writable value, int numReduceTasks)
    {
        String urlString = key.toString();
        URL url;
        int hashCode = urlString.hashCode();

        try
        {
            urlString = normalizers.normalize(urlString, URLNormalizers.SCOPE_PARTITION);
            url = new URL(urlString);
            hashCode = url.getHost().hashCode();

            switch (mode)
            {
                case PARTITION_MODE_DOMAIN:
                    hashCode = URLUtil.getDomainName(url).hashCode();
                    break;
                case PARTITION_MODE_IP:
                    try
                    {
                        InetAddress address = InetAddress.getByName(url.getHost());
                        hashCode = address.getHostAddress().hashCode();
                    }
                    catch (UnknownHostException e)
                    {
                        LOG.info("Can't resolv host: " + url.getHost());
                    }
                    break;
            }
        }
        catch (MalformedURLException e)
        {
            LOG.warn("Malformed URL: " + urlString);
        }

        // make hosts wind up in different partitions on different runs
        hashCode ^= seed;

        return (hashCode & Integer.MAX_VALUE) % numReduceTasks;
    }
}
