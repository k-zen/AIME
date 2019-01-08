package io.aime.util;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

/**
 * Utility to create a Hadoop's {@link Configuration} object that include AIME
 * specific resources.
 *
 * @author K-Zen
 */
public class AIMEConfiguration {

    public Configuration create() {
        Configuration conf = new Configuration();
        this.addAIMEResources(conf);

        return conf;
    }

    public Configuration addAIMEResources(Configuration conf) {
        conf.addResource("aime-default.xml");
        conf.addResource("aime-site.xml");

        return conf;
    }
}
