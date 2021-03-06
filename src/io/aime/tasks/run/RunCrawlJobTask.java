package io.aime.tasks.run;

import io.aime.brain.data.MetadataCrawlJob;
import net.apkc.emma.tasks.Task;
import org.apache.log4j.Logger;

/**
 * This is a task that launches a new or resuming crawl job.
 *
 * @author K-Zen
 */
public class RunCrawlJobTask extends Task
{

    private static final Logger LOG = Logger.getLogger(RunCrawlJobTask.class.getName());
    private MetadataCrawlJob.Data job = (MetadataCrawlJob.Data) MetadataCrawlJob.getInstance().getEmptyData();
    private int depth = 0;
    private boolean sendmail = false;

    private RunCrawlJobTask()
    {
    }

    public static RunCrawlJobTask newBuild()
    {
        return new RunCrawlJobTask();
    }

    public MetadataCrawlJob.Data getJob()
    {
        return job;
    }

    public RunCrawlJobTask setJob(Object job)
    {
        if (!MetadataCrawlJob.Data.class.isInstance(job)) {
            LOG.warn("Wrong object passed to RunCrawlJobTask.");
            return this;
        }
        this.job = (MetadataCrawlJob.Data) job;
        return this;
    }

    public RunCrawlJobTask setDepth(Object depth)
    {
        if (!String.class.isInstance(depth)) {
            LOG.warn("Wrong object passed to LaunchCrawlJobTask.");
            return this;
        }
        try {
            this.depth = Integer.parseInt((String) depth);
        }
        catch (Exception e) {
            LOG.error("Can't parse depth value! Error: " + e.toString(), e);
            this.depth = 50;
        }
        return this;
    }

    public RunCrawlJobTask setSendmail(boolean sendmail)
    {
        this.sendmail = sendmail;
        return this;
    }

    @Override
    protected Object doInBackground() throws Exception
    {
        job.runJob(depth, sendmail, this); // Start the crawling job.
        return null;
    }

    @Override
    public void reportProgress(int progress)
    {
        setProgress(progress);
    }
}
