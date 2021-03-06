package io.aime.util;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;

/**
 * This class extends JobConf and creates a custom Job configuration.
 *
 * @author Nutch.org
 * @author K-Zen
 */
public class AIMEJob extends JobConf {

    public AIMEJob(Configuration conf) {
        super(conf, AIMEJob.class);
    }

    public AIMEJob(Configuration conf, String jobName) {
        super(conf, AIMEJob.class);
        this.setJobName(jobName);
    }
}
