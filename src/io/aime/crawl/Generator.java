package io.aime.crawl;

import io.aime.brain.Brain;
import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.MetadataGeneral;
import io.aime.brain.xml.Handler;
import io.aime.net.URLFilter;
import io.aime.net.URLFilterException;
import io.aime.net.URLFilters;
import io.aime.net.URLNormalizers;
import io.aime.util.AIMEConfiguration;
import io.aime.util.AIMEConstants;
import io.aime.util.AIMEJob;
import io.aime.util.LockUtil;
import io.aime.util.ProcessKiller;
import io.aime.util.URLUtil;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.SwingWorker;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapFileOutputFormat;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Partitioner;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.mapred.lib.MultipleSequenceFileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

/**
 * Generates a subset of a crawl db to fetch.
 *
 * <p>
 * This version allows to generate fetchlists for several segments in one
 * go.</p>
 * <p>
 * Unlike in the initial version (OldGenerator), the IP resolution is done
 * ONLY on the entries which have been selected for fetching. The URLs are
 * partitioned by IP, domain or host within a segment.</p>
 * <p>
 * We can chose separately how to count the URLS i.e. by domain or host to
 * limit the entries.</p>
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class Generator extends Configured implements Tool
{

    private static final Logger LOG = Logger.getLogger(Generator.class.getName());
    private static final String GENERATE_UPDATE_CRAWLDB = "generate.update.crawldb";
    private static final String GENERATOR_MIN_SCORE = "generate.min.score";
    private static final String GENERATOR_FILTER = "generate.filter";
    private static final String GENERATOR_NORMALISE = "generate.normalise";
    private static final String GENERATOR_MAX_COUNT = "generate.max.count";
    private static final String GENERATOR_COUNT_MODE = "generate.count.mode";
    private static final String GENERATOR_COUNT_VALUE_DOMAIN = "domain";
    private static final String GENERATOR_TOP_N = "generate.topN";
    private static final String GENERATOR_CUR_TIME = "generate.curTime";
    private static final String GENERATOR_DELAY = "crawl.gen.delay";
    private static final String GENERATOR_MAX_NUM_SEGMENTS = "generate.max.num.segments";
    private static final String GENERATE_MAX_PER_HOST_BY_IP = "generate.max.per.host.by.ip";
    private static final String GENERATE_MAX_PER_HOST = "generate.max.per.host";
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    public Generator()
    {
    }

    public Generator(Configuration conf)
    {
        super.setConf(conf);
    }

    /**
     * Serializable class that wraps generated entries.
     */
    public static class SelectorEntry implements Writable
    {

        public Text url;
        public CrawlDatum datum;
        public IntWritable segnum;

        public SelectorEntry()
        {
            url = new Text();
            datum = new CrawlDatum();
            segnum = new IntWritable(0);
        }

        @Override
        public void readFields(DataInput in) throws IOException
        {
            url.readFields(in);
            datum.readFields(in);
            segnum.readFields(in);
        }

        @Override
        public void write(DataOutput out) throws IOException
        {
            url.write(out);
            datum.write(out);
            segnum.write(out);
        }

        @Override
        public String toString()
        {
            return "url=" + url.toString() + ", datum=" + datum.toString() + ", segnum=" + segnum.toString();
        }
    }

    /**
     * Selects entries due for fetch.
     */
    public static class Selector implements Mapper<Text, CrawlDatum, FloatWritable, SelectorEntry>, Partitioner<FloatWritable, Writable>, Reducer<FloatWritable, SelectorEntry, FloatWritable, SelectorEntry>
    {

        private LongWritable genTime = new LongWritable(System.currentTimeMillis());
        private long curTime;
        private long limit;
        private long count;
        private HashMap<String, int[]> hostCounts = new HashMap<>();
        private int maxCount;
        private boolean byDomain = false;
        private Partitioner<Text, Writable> partitioner = new URLPartitioner();
        private URLFilter[] filters;
        private URLNormalizers normalizers;
        private SelectorEntry entry = new SelectorEntry();
        private FloatWritable sortValue = new FloatWritable();
        private boolean filter;
        private boolean normalize;
        private long genDelay;
        private FetchSchedule schedule;
        private float scoreThreshold = 0.0f;
        private int maxNumSegments = 1;
        int currentsegmentnum = 1;

        @Override
        public void configure(JobConf job)
        {
            curTime = job.getLong(GENERATOR_CUR_TIME, System.currentTimeMillis());
            limit = job.getLong(GENERATOR_TOP_N, Long.MAX_VALUE) / job.getNumReduceTasks();
            maxCount = job.getInt(GENERATOR_MAX_COUNT, -1);

            // back compatibility with old param
            int oldMaxPerHost = job.getInt(GENERATE_MAX_PER_HOST, -1);

            if (maxCount == -1 && oldMaxPerHost != -1) {
                maxCount = oldMaxPerHost;
                byDomain = false;
            }

            if (GENERATOR_COUNT_VALUE_DOMAIN.equals(job.get(GENERATOR_COUNT_MODE))) {
                byDomain = true;
            }

            filters = (URLFilter[]) Brain
                    .getClient(job)
                    .execute(Handler
                            .makeXMLRequest(BrainXMLData
                                    .newBuild()
                                    .setJob(BrainXMLData.JOB_REQUEST)
                                    .setClazz(MetadataGeneral.Data.class)
                                    .setFunction(MetadataGeneral.Data.FILTERS.getMethodName()))).get();
            normalize = job.getBoolean(GENERATOR_NORMALISE, true);

            if (normalize) {
                normalizers = new URLNormalizers(job, URLNormalizers.SCOPE_GENERATE_HOST_COUNT);
            }

            partitioner.configure(job);
            filter = job.getBoolean(GENERATOR_FILTER, true);
            genDelay = job.getLong(GENERATOR_DELAY, 7L) * 3600L * 24L * 1000L;

            long time = job.getLong(AIMEConstants.GENERATE_TIME_KEY.getStringConstant(), 0L);
            if (time > 0) {
                genTime.set(time);
            }

            schedule = FetchScheduleFactory.getFetchSchedule(job);
            scoreThreshold = job.getFloat(GENERATOR_MIN_SCORE, Float.NaN);
            maxNumSegments = job.getInt(GENERATOR_MAX_NUM_SEGMENTS, 1);
        }

        @Override
        public void close()
        {
        }

        @Override
        public void map(Text key, CrawlDatum value, OutputCollector<FloatWritable, SelectorEntry> output, Reporter reporter) throws IOException
        {
            Text url = key;

            if (filter) {
                // If filtering is on don't generate URLs that don't pass
                // URLFilters
                try {
                    if (URLFilters.filter(filters, url.toString()) == null) {
                        return;
                    }
                }
                catch (URLFilterException e) {
                    LOG.warn("Can't filter URL: " + url + " (" + e.getMessage() + ")");
                }
            }

            CrawlDatum crawlDatum = value;

            // check fetch schedule
            if (!schedule.shouldFetch(url, crawlDatum, curTime)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Rejected: [shouldFetch] '" + url + "', [fetchTime] '" + crawlDatum.getFetchTime() + "', [curTime] '" + curTime + "'");
                }

                return;
            }

            LongWritable oldGenTime = (LongWritable) crawlDatum.getMetadata().get(AIMEConstants.WRITABLE_GENERATE_TIME_KEY.getTextConstant());

            if (oldGenTime != null) { // awaiting fetch & update
                if (oldGenTime.get() + genDelay > curTime) { // still wait for
                    // update
                    return;
                }
            }

            float sort = crawlDatum.getScore() * 1.0f;

            // consider only entries with a score superior to the threshold
            if (scoreThreshold != Float.NaN && sort < scoreThreshold) {
                return;
            }

            // sort by decreasing score, using DecreasingFloatComparator
            sortValue.set(sort);
            // record generation time
            crawlDatum.getMetadata().put(AIMEConstants.WRITABLE_GENERATE_TIME_KEY.getTextConstant(), genTime);
            entry.datum = crawlDatum;
            entry.url = key;
            output.collect(sortValue, entry); // invert for sort by score
        }

        /**
         * Partition by host / domain or IP.
         *
         * @param key
         * @param value
         * @param numReduceTasks
         *
         * @return
         */
        @Override
        public int getPartition(FloatWritable key, Writable value, int numReduceTasks)
        {
            return partitioner.getPartition(((SelectorEntry) value).url, key, numReduceTasks);
        }

        /**
         * Collect until limit is reached.
         *
         * @param key
         * @param values
         * @param output
         * @param reporter
         *
         * @throws IOException
         */
        @Override
        public void reduce(FloatWritable key, Iterator<SelectorEntry> values, OutputCollector<FloatWritable, SelectorEntry> output, Reporter reporter) throws IOException
        {
            while (values.hasNext()) {
                if (count == limit) {
                    // do we have any segments left?
                    if (currentsegmentnum < maxNumSegments) {
                        count = 0;
                        currentsegmentnum++;
                    }
                    else {
                        break;
                    }
                }

                SelectorEntry etr = values.next();
                Text url = etr.url;
                String urlString = url.toString();
                URL u;
                String hostordomain;

                try {
                    if (normalize && normalizers != null) {
                        urlString = normalizers.normalize(urlString, URLNormalizers.SCOPE_GENERATE_HOST_COUNT);
                    }

                    u = new URL(urlString);

                    if (byDomain) {
                        hostordomain = URLUtil.getDomainName(u);
                    }
                    else {
                        hostordomain = new URL(urlString).getHost();
                    }
                }
                catch (Exception e) {
                    LOG.warn("Malformed URL: '" + urlString + "', skipping [" + StringUtils.stringifyException(e) + "]");
                    continue;
                }

                hostordomain = hostordomain.toLowerCase();

                // only filter if we are counting hosts or domains
                if (maxCount > 0) {
                    int[] hostCount = hostCounts.get(hostordomain);
                    if (hostCount == null) {
                        hostCount = new int[]{
                            1, 0
                        };
                        hostCounts.put(hostordomain, hostCount);
                    }

                    // increment hostCount
                    hostCount[1]++;

                    // reached the limit of allowed URLs per host / domain
                    // see if we can put it in the next segment?
                    if (hostCount[1] > maxCount) {
                        if (hostCount[0] < maxNumSegments) {
                            hostCount[0]++;
                            hostCount[1] = 0;
                        }
                        else {
                            if (hostCount[1] == maxCount + 1 && LOG.isInfoEnabled()) {
                                LOG.info("Host or domain: " + hostordomain + " has more than [" + maxCount + "] URLs for all [" + maxNumSegments + "] segments, skipping.");
                            }

                            // skip this entry
                            continue;
                        }
                    }
                    etr.segnum = new IntWritable(hostCount[0]);
                }
                else {
                    etr.segnum = new IntWritable(currentsegmentnum);
                }

                output.collect(key, etr);

                // Count is incremented only when we keep the URL
                // maxCount may cause us to skip it.
                count++;
            }
        }
    }

    public static class GeneratorOutputFormat extends MultipleSequenceFileOutputFormat<FloatWritable, SelectorEntry>
    {

        @Override
        protected String generateFileNameForKeyValue(FloatWritable key, SelectorEntry value, String name)
        {
            return "fetchlist-" + value.segnum.toString() + "/" + name;
        }
    }

    public static class DecreasingFloatComparator extends FloatWritable.Comparator
    {

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2)
        {
            return super.compare(b2, s2, l2, b1, s1, l1);
        }
    }

    public static class SelectorInverseMapper extends MapReduceBase implements Mapper<FloatWritable, SelectorEntry, Text, SelectorEntry>
    {

        @Override
        public void map(FloatWritable key, SelectorEntry value, OutputCollector<Text, SelectorEntry> output, Reporter reporter) throws IOException
        {
            SelectorEntry entry = value;
            output.collect(entry.url, entry);
        }
    }

    public static class PartitionReducer extends MapReduceBase implements Reducer<Text, SelectorEntry, Text, CrawlDatum>
    {

        @Override
        public void reduce(Text key, Iterator<SelectorEntry> values, OutputCollector<Text, CrawlDatum> output, Reporter reporter) throws IOException
        {
            // if using HashComparator, we get only one input key in case of hash collision
            // so use only URLs from values
            while (values.hasNext()) {
                SelectorEntry entry = values.next();
                output.collect(entry.url, entry.datum);
            }
        }
    }

    /**
     * Sort fetch lists by hash of URL.
     */
    public static class HashComparator extends WritableComparator
    {

        public HashComparator()
        {
            super(Text.class);
        }

        @Override
        public int compare(WritableComparable a, WritableComparable b)
        {
            Text url1 = (Text) a;
            Text url2 = (Text) b;
            int hash1 = hash(url1.getBytes(), 0, url1.getLength());
            int hash2 = hash(url2.getBytes(), 0, url2.getLength());

            return (hash1 < hash2 ? -1 : (hash1 == hash2 ? 0 : 1));
        }

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2)
        {
            int hash1 = hash(b1, s1, l1);
            int hash2 = hash(b2, s2, l2);

            return (hash1 < hash2 ? -1 : (hash1 == hash2 ? 0 : 1));
        }

        private static int hash(byte[] bytes, int start, int length)
        {
            int hash = 1;
            // make later bytes more significant in hash code, so that sorting by
            // hashcode correlates less with by-host ordering.
            for (int i = length - 1; i >= 0; i--) {
                hash = (31 * hash) + (int) bytes[start + i];
            }

            return hash;
        }
    }

    /**
     * Update the CrawlDB so that the next generate won't include the same URLs.
     */
    public static class CrawlDbUpdater extends MapReduceBase implements Mapper<Text, CrawlDatum, Text, CrawlDatum>, Reducer<Text, CrawlDatum, Text, CrawlDatum>
    {

        private CrawlDatum orig = new CrawlDatum();
        private LongWritable genTime = new LongWritable(0L);
        long generateTime;

        @Override
        public void configure(JobConf job)
        {
            generateTime = job.getLong(AIMEConstants.GENERATE_TIME_KEY.getStringConstant(), 0L);
        }

        @Override
        public void map(Text key, CrawlDatum value, OutputCollector<Text, CrawlDatum> output, Reporter reporter) throws IOException
        {
            output.collect(key, value);
        }

        @Override
        public void reduce(Text key, Iterator<CrawlDatum> values, OutputCollector<Text, CrawlDatum> output, Reporter reporter) throws IOException
        {
            genTime.set(0L);

            while (values.hasNext()) {
                CrawlDatum val = values.next();
                if (val.getMetadata().containsKey(AIMEConstants.WRITABLE_GENERATE_TIME_KEY.getTextConstant())) {
                    LongWritable gt = (LongWritable) val.getMetadata().get(AIMEConstants.WRITABLE_GENERATE_TIME_KEY.getTextConstant());
                    genTime.set(gt.get());
                    if (genTime.get() != generateTime) {
                        orig.set(val);
                        genTime.set(0L);
                    }
                }
                else {
                    orig.set(val);
                }
            }

            if (genTime.get() != 0L) {
                orig.getMetadata().put(AIMEConstants.WRITABLE_GENERATE_TIME_KEY.getTextConstant(), genTime);
            }

            output.collect(key, orig);
        }
    }

    /**
     * Generate fetch lists in one or more segments. Whether to filter URLs or
     * not is read from the crawl.generate.filter property in the configuration
     * files. If the property is not found, the URLs are filtered. Same for the
     * normalization.
     *
     * @param dbDir          Crawl database directory
     * @param segments       Segments directory
     * @param numLists       Number of reduce tasks
     * @param topN           Number of top URLs to be selected
     * @param curTime        Current time in milliseconds
     * @param filter
     * @param norm
     * @param force
     * @param maxNumSegments
     *
     * @return 0 if all was correct, &gt;0 if an error occurred.
     *
     * @throws IOException - When an I/O error occurs
     */
    public int generate(Path dbDir, Path segments, int numLists, long topN, long curTime, boolean filter, boolean norm, boolean force, int maxNumSegments) throws IOException
    {
        Path tempDir1 = new Path(getConf().get("mapred.temp.dir", ".") + "/generate-temp1-" + System.currentTimeMillis());
        Path tempDir2 = new Path(getConf().get("mapred.temp.dir", ".") + "/generate-temp2-" + System.currentTimeMillis()); // Update DB from tempDir
        Path lock = new Path(dbDir, CrawlDB.LOCK_NAME);
        FileSystem fs = FileSystem.get(getConf());
        LockUtil.createLockFile(fs, lock, force);
        JobConf job;
        long generateTime = System.currentTimeMillis();
        int res = 0;

        if (LOG.isInfoEnabled()) {
            LOG.info("Should Filter: " + filter);
            LOG.info("Should Normalize: " + norm);
        }

        if (topN != Long.MAX_VALUE && LOG.isInfoEnabled()) {
            LOG.info("Top URLs: " + topN);
        }

        if ("true".equals(getConf().get(GENERATE_MAX_PER_HOST_BY_IP)) && LOG.isDebugEnabled()) {
            LOG.debug("[GENERATE_MAX_PER_HOST_BY_IP] will be ignored, use [partition.url.mode].");
        }

        try {
            // Create job
            job = new AIMEJob(getConf());
            // Configure
            job.setJobName(AIMEConstants.GENERATOR_JOB_NAME.getStringConstant() + "#1");
            if (numLists == -1) { // for politeness make
                numLists = job.getNumMapTasks(); // a partition per fetch task
            }
            if ("local".equals(job.get("mapred.job.tracker")) && numLists != 1) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("JobTracker is local, generating only one partition.");
                }
                numLists = 1;
            }
            job.setLong(GENERATOR_CUR_TIME, curTime);
            job.setLong(AIMEConstants.GENERATE_TIME_KEY.getStringConstant(), generateTime);
            job.setLong(GENERATOR_TOP_N, topN);
            job.setBoolean(GENERATOR_FILTER, filter);
            job.setBoolean(GENERATOR_NORMALISE, norm);
            job.setInt(GENERATOR_MAX_NUM_SEGMENTS, maxNumSegments);
            job.setInputFormat(SequenceFileInputFormat.class);
            job.setMapperClass(Selector.class);
            job.setPartitionerClass(Selector.class);
            job.setReducerClass(Selector.class);
            job.setOutputFormat(SequenceFileOutputFormat.class);
            job.setOutputKeyClass(FloatWritable.class);
            job.setOutputKeyComparatorClass(DecreasingFloatComparator.class);
            job.setOutputValueClass(SelectorEntry.class);
            job.setOutputFormat(GeneratorOutputFormat.class);
            // IO paths
            FileInputFormat.addInputPath(job, new Path(dbDir, CrawlDB.CURRENT_NAME));
            FileOutputFormat.setOutputPath(job, tempDir1);
            // Run
            JobClient.runJob(job);

            // read the subdirectories generated in the temp
            // output and turn them into segments
            List<Path> generatedSegments = new ArrayList<>();
            FileStatus[] status = fs.listStatus(tempDir1);
            try {
                for (FileStatus stat : status) {
                    Path subfetchlist = stat.getPath();
                    if (!subfetchlist.getName().startsWith("fetchlist-")) {
                        continue;
                    }
                    // start a new partition job for this segment
                    Path newSeg = partitionSegment(fs, segments, subfetchlist, numLists);
                    generatedSegments.add(newSeg);
                }
            }
            catch (Exception e) {
                LOG.fatal("Fatal error partitioning the segments, trying to abort process ...", e);
                res += 1;
            }

            if (generatedSegments.isEmpty()) {
                LOG.warn("0 links selected for fetching, trying to abort process ...");
                res += 1;
            }

            if (getConf().getBoolean(GENERATE_UPDATE_CRAWLDB, false)) {
                // Create job
                job = new AIMEJob(getConf());
                // Configure
                job.setJobName(AIMEConstants.GENERATOR_JOB_NAME.getStringConstant() + "#2");
                job.setLong(AIMEConstants.GENERATE_TIME_KEY.getStringConstant(), generateTime);
                job.setInputFormat(SequenceFileInputFormat.class);
                job.setMapperClass(CrawlDbUpdater.class);
                job.setReducerClass(CrawlDbUpdater.class);
                job.setOutputFormat(MapFileOutputFormat.class);
                job.setOutputKeyClass(Text.class);
                job.setOutputValueClass(CrawlDatum.class);
                // IO paths
                for (Path segmpaths : generatedSegments) {
                    Path subGenDir = new Path(segmpaths, CrawlDatum.GENERATE_DIR_NAME);
                    FileInputFormat.addInputPath(job, subGenDir);
                }
                FileInputFormat.addInputPath(job, new Path(dbDir, CrawlDB.CURRENT_NAME));
                FileOutputFormat.setOutputPath(job, tempDir2);
                // Run & Install
                JobClient.runJob(job);
                CrawlDB.install(job, dbDir);
            }
        }
        catch (Exception e) {
            LOG.fatal("Error generating segments. Error: " + e.toString(), e);
            res += 1;
        }
        finally {
            if (LOG.isInfoEnabled()) {
                LOG.info("Cleaning temporary files.");
            }
            LockUtil.removeLockFile(fs, lock);
            fs.delete(tempDir1, true);
            fs.delete(tempDir2, true);

            return res;
        }
    }

    private Path partitionSegment(FileSystem fs, Path segmentsDir, Path inputDir, int numLists) throws IOException
    {
        if (LOG.isInfoEnabled()) {
            LOG.info("Partitioning URLs for courtesy.");
        }

        Path segment = new Path(segmentsDir, generateSegmentName());
        Path output = new Path(segment, CrawlDatum.GENERATE_DIR_NAME);

        if (LOG.isInfoEnabled()) {
            LOG.info("Segment: " + segment);
        }

        // Create job
        AIMEJob job = new AIMEJob(getConf());
        // Configure
        job.setJobName(AIMEConstants.GENERATOR_JOB_NAME.getStringConstant() + "#3");
        job.setInt("partition.url.seed", new Random().nextInt());
        job.setInputFormat(SequenceFileInputFormat.class);
        job.setMapperClass(SelectorInverseMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(SelectorEntry.class);
        job.setPartitionerClass(URLPartitioner.class);
        job.setReducerClass(PartitionReducer.class);
        job.setNumReduceTasks(numLists);
        job.setOutputFormat(SequenceFileOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(CrawlDatum.class);
        job.setOutputKeyComparatorClass(HashComparator.class);
        // IO paths
        FileInputFormat.addInputPath(job, inputDir);
        FileOutputFormat.setOutputPath(job, output);
        // Run
        JobClient.runJob(job);

        return segment;
    }

    public static synchronized String generateSegmentName()
    {
        try {
            Thread.sleep(1000);
        }
        catch (Throwable t) {
            LOG.error("Error generating segment name. Error: " + t.toString(), t);
        }

        return sdf.format(new Date(System.currentTimeMillis()));
    }

    /**
     * Launch the process.
     *
     * @param args   Arguments.
     * @param runner The launching thread.
     *
     * @return
     *
     * @throws java.lang.Exception
     */
    public static int runProcess(String args[], SwingWorker<?, ?> runner) throws Exception
    {
        Configuration conf = new AIMEConfiguration().create();
        return ProcessKiller.checkExitCode(
                ToolRunner.run(conf, new Generator(), args),
                AIMEConstants.GENERATOR_JOB_NAME.getStringConstant(),
                runner,
                conf);
    }

    @Override
    public int run(String[] args) throws Exception
    {
        Path dbDir = new Path(args[0]);
        Path segmentsDir = new Path(args[1]);
        long curTime = System.currentTimeMillis();
        // El buffer consiste en la cantidad de URLs a buscar con cada iteracion del Fetcher.
        // Cuando de llena el buffer entonces se corta la busqueda y se procesan los resultados.
        // Por defecto el tamaño del buffer se calcula con la formula: Buffer = ThreadCount * DampingFactor
        long topN = getConf().getInt("fetcher.threads.fetch", 10) * getConf().getInt("fetcher.buffer.queue.dampingfactor", 100);
        int numFetchers = -1;
        boolean filter = true;
        boolean norm = true;
        boolean force = false;
        int maxNumSegments = 1;

        try {
            if (generate(dbDir, segmentsDir, numFetchers, topN, curTime, filter, norm, force, maxNumSegments) > 0) {
                return -1;
            }
        }
        catch (Exception e) {
            LOG.fatal(StringUtils.stringifyException(e));
            return -1;
        }

        return 0;
    }
}
