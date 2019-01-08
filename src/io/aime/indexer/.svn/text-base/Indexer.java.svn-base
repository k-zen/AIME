package io.aime.indexer;

// AIME
import io.aime.util.AIMEConfiguration;
import io.aime.util.AIMEConstants;
import io.aime.util.AIMEJob;
import io.aime.util.ProcessKiller;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

// IO
import java.io.IOException;

// Log4j
import org.apache.log4j.Logger;

// Swing
import javax.swing.SwingWorker;

public class Indexer extends Configured implements Tool {

    private static final Logger LOG = Logger.getLogger(Indexer.class.getName());
    private int exeType;

    private Indexer(int exeType) {
        super(new AIMEConfiguration().create());
        this.exeType = exeType;
    }

    private Indexer(Configuration conf) {
        super(conf);
    }

    private void index() throws IOException {
        // Create job
        JobConf job = new AIMEJob(getConf(), AIMEConstants.INDEXER_JOB_NAME.getStringConstant());
        // Configure
        IndexerMapReduce.configureIndexer(job, this.exeType);
        // Run the job.
        JobClient.runJob(job);
    }

    @Override
    public int run(String[] args) throws Exception {
        try {
            index();
            return 0;
        }
        catch (final Exception e) {
            LOG.fatal(StringUtils.stringifyException(e));
            return -1;
        }
    }

    /**
     * Run the MR job.
     *
     * @param args    Arguments
     * @param runner  The thread that launched the process
     * @param exeType Execution type
     *
     * @return 0 if the process was OK, different than 0 otherwise.
     *
     * @throws Exception A generic exception
     */
    public static int runProcess(String[] args, SwingWorker<?, ?> runner, int exeType) throws Exception {
        Configuration conf = new AIMEConfiguration().create();
        return ProcessKiller.checkExitCode(
                ToolRunner.run(conf, new Indexer(exeType), args),
                AIMEConstants.INDEXER_JOB_NAME.getStringConstant(),
                runner,
                conf);
    }
}
