package io.aime.crawl;

import io.aime.util.AIMEConfiguration;
import io.aime.util.AIMEConstants;
import io.aime.util.AIMEJob;
import io.aime.util.HadoopFSUtil;
import io.aime.util.LockUtil;
import io.aime.util.ProcessKiller;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import javax.swing.SwingWorker;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapFileOutputFormat;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

/**
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class CrawlDB extends Configured implements Tool
{

    private static final Logger LOG = Logger.getLogger(CrawlDB.class.getName());
    public static final String CRAWLDB_ADDITIONS_ALLOWED = "db.update.additions.allowed";
    public static final String CURRENT_NAME = "current";
    public static final String LOCK_NAME = ".locked";

    public CrawlDB()
    {
    }

    public CrawlDB(Configuration conf)
    {
        setConf(conf);
    }

    public void update(Path crawlDb, Path[] segments, boolean normalize, boolean filter) throws IOException
    {
        boolean additionsAllowed = getConf().getBoolean(CRAWLDB_ADDITIONS_ALLOWED, true);
        update(crawlDb, segments, normalize, filter, additionsAllowed, false);
    }

    public void update(Path crawlDb, Path[] segments, boolean normalize, boolean filter, boolean additionsAllowed, boolean force) throws IOException
    {
        if (LOG.isInfoEnabled())
        {
            LOG.info("DBase: " + crawlDb);
            LOG.info("Segments: " + Arrays.asList(segments));
            LOG.info("Permited additions: " + additionsAllowed);
            LOG.info("URL Normalization: " + normalize);
            LOG.info("URL Filtering: " + filter);
        }

        FileSystem fs = FileSystem.get(getConf());
        Path lock = new Path(crawlDb, LOCK_NAME);
        LockUtil.createLockFile(fs, lock, force);

        JobConf job = CrawlDB.createJob(getConf(), crawlDb);
        job.setBoolean(CRAWLDB_ADDITIONS_ALLOWED, additionsAllowed);
        job.setBoolean(CrawlDBFilter.URL_FILTERING, filter);
        job.setBoolean(CrawlDBFilter.URL_NORMALIZING, normalize);

        for (Path segment : segments)
        {
            Path fetch = new Path(segment, CrawlDatum.FETCH_DIR_NAME);
            Path parse = new Path(segment, CrawlDatum.PARSE_DIR_NAME);
            if (fs.exists(fetch) && fs.exists(parse))
            {
                FileInputFormat.addInputPath(job, fetch);
                FileInputFormat.addInputPath(job, parse);
            }
            else
            {
                if (LOG.isInfoEnabled())
                {
                    LOG.info("Invalid segment skipping: " + segment);
                }
            }
        }

        if (LOG.isInfoEnabled())
        {
            LOG.info("Adding data segment to DBase.");
        }

        try
        {
            JobClient.runJob(job);
        }
        catch (IOException e)
        {
            LockUtil.removeLockFile(fs, lock);
            Path outPath = FileOutputFormat.getOutputPath(job);

            if (fs.exists(outPath))
            {
                fs.delete(outPath, true);
            }

            throw e;
        }

        CrawlDB.install(job, crawlDb);
    }

    public static JobConf createJob(Configuration config, Path crawlDb) throws IOException
    {
        Path newCrawlDb = new Path(crawlDb, Integer.toString(new Random().nextInt(Integer.MAX_VALUE)));
        Path current = new Path(crawlDb, CURRENT_NAME);

        // Create job
        JobConf job = new AIMEJob(config);
        // Configure
        job.setJobName("Main_DBase_Updater");
        job.setInputFormat(SequenceFileInputFormat.class);
        job.setMapperClass(CrawlDBFilter.class);
        job.setReducerClass(CrawlDBReducer.class);
        job.setOutputFormat(MapFileOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(CrawlDatum.class);
        // IO paths
        if (FileSystem.get(job).exists(current))
        {
            FileInputFormat.addInputPath(job, current);
        }
        FileOutputFormat.setOutputPath(job, newCrawlDb);

        return job;
    }

    public static void install(JobConf job, Path crawlDb) throws IOException
    {
        Path newCrawlDb = FileOutputFormat.getOutputPath(job);
        FileSystem fs = new JobClient(job).getFs();
        Path old = new Path(crawlDb, "old");
        Path current = new Path(crawlDb, CURRENT_NAME);

        if (fs.exists(current))
        {
            if (fs.exists(old))
            {
                fs.delete(old, true);
            }

            fs.rename(current, old);
        }

        fs.mkdirs(crawlDb);
        fs.rename(newCrawlDb, current);

        if (fs.exists(old))
        {
            fs.delete(old, true);
        }

        Path lock = new Path(crawlDb, LOCK_NAME);
        LockUtil.removeLockFile(fs, lock);
    }

    public static int runProcess(String[] args, SwingWorker<?, ?> runner) throws Exception
    {
        Configuration conf = new AIMEConfiguration().create();
        return ProcessKiller.checkExitCode(
                ToolRunner.run(conf, new CrawlDB(), args),
                AIMEConstants.UPDATE_JOB_NAME.getStringConstant(),
                runner,
                conf);
    }

    @Override
    public int run(String[] args) throws Exception
    {
        boolean normalize = false;
        boolean filter = false;
        boolean force = false;
        final FileSystem fs = FileSystem.get(getConf());
        boolean additionsAllowed = getConf().getBoolean(CRAWLDB_ADDITIONS_ALLOWED, true);
        HashSet<Path> dirs = new HashSet<>();

        for (int i = 1; i < args.length; i++)
        {
            switch (args[i])
            {
                case "-normalize":
                    normalize = true;
                    break;
                case "-filter":
                    filter = true;
                    break;
                case "-force":
                    force = true;
                    break;
                case "-noAdditions":
                    additionsAllowed = false;
                    break;
                case "-dir":
                    FileStatus[] paths = fs.listStatus(new Path(args[++i]), HadoopFSUtil.getPassDirectoriesFilter(fs));
                    dirs.addAll(Arrays.asList(HadoopFSUtil.getPaths(paths)));
                    break;
                default:
                    dirs.add(new Path(args[i]));
                    break;
            }
        }

        try
        {
            update(new Path(args[0]), dirs.toArray(new Path[dirs.size()]), normalize, filter, additionsAllowed, force);
            return 0;
        }
        catch (Exception e)
        {
            LOG.fatal("General error. Error: " + e.toString(), e);
            return -1;
        }
    }
}
