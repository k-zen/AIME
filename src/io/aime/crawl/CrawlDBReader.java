package io.aime.crawl;

import io.aime.util.AIMEJob;
import io.aime.util.GeneralUtilities;
import io.aime.util.HtmlMessageBuilder;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Closeable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapFileOutputFormat;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.HashPartitioner;
import org.apache.hadoop.mapred.lib.IdentityMapper;
import org.apache.hadoop.mapred.lib.IdentityReducer;
import org.apache.hadoop.util.Progressable;
import org.apache.log4j.Logger;

/**
 * Read utility for the Main DBase.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class CrawlDBReader implements Closeable
{

    private static final Logger LOG = Logger.getLogger(CrawlDBReader.class.getName());
    private static final CrawlDBReader _INSTANCE = new CrawlDBReader();
    private static final int STD_FORMAT = 0;
    private static final int CSV_FORMAT = 1;
    private MapFile.Reader[] readers = null;

    private CrawlDBReader()
    {
    }

    public static CrawlDBReader getInstance()
    {
        return _INSTANCE;
    }

    private void openReaders(String crawlDb, Configuration config) throws IOException
    {
        if (readers != null)
        {
            return;
        }

        FileSystem fs = FileSystem.get(config);
        readers = MapFileOutputFormat.getReaders(fs, new Path(crawlDb, CrawlDB.CURRENT_NAME), config);
    }

    private void closeReaders()
    {
        if (readers == null)
        {
            return;
        }

        for (MapFile.Reader reader : readers)
        {
            try
            {
                reader.close();
            }
            catch (Exception e)
            {
                // #TODO: Do something!
            }
        }
    }

    public static class CrawlDatumCsvOutputFormat extends FileOutputFormat<Text, CrawlDatum>
    {

        protected static class LineRecordWriter implements RecordWriter<Text, CrawlDatum>
        {

            private DataOutputStream out;

            public LineRecordWriter(DataOutputStream out)
            {
                this.out = out;
                try
                {
                    out.writeBytes("Url;Status code;Status name;Fetch Time;Modified Time;Retries since fetch;Retry interval;Score;Signature;Metadata\n");
                }
                catch (IOException e)
                {
                }
            }

            @Override
            public synchronized void write(Text key, CrawlDatum value) throws IOException
            {
                out.writeByte('"');
                out.writeBytes(key.toString());
                out.writeByte('"');
                out.writeByte(';');
                out.writeBytes(Integer.toString(value.getStatus()));
                out.writeByte(';');
                out.writeByte('"');
                out.writeBytes(CrawlDatum.getStatusName(value.getStatus()));
                out.writeByte('"');
                out.writeByte(';');
                out.writeBytes(new Date(value.getFetchTime()).toString());
                out.writeByte(';');
                out.writeBytes(new Date(value.getModifiedTime()).toString());
                out.writeByte(';');
                out.writeBytes(Integer.toString(value.getRetriesSinceFetch()));
                out.writeByte(';');
                out.writeBytes(Float.toString(value.getFetchInterval()));
                out.writeByte(';');
                out.writeBytes(Float.toString((value.getFetchInterval() / FetchSchedule.SECONDS_PER_DAY)));
                out.writeByte(';');
                out.writeBytes(Float.toString(value.getScore()));
                out.writeByte(';');
                out.writeByte('"');
                out.writeBytes(Long.toString(value.getSignature()));
                out.writeByte('"');
                out.writeByte('\n');
            }

            @Override
            public synchronized void close(Reporter reporter) throws IOException
            {
                out.close();
            }
        }

        @Override
        public RecordWriter<Text, CrawlDatum> getRecordWriter(FileSystem fs, JobConf job, String name, Progressable progress) throws IOException
        {
            Path dir = FileOutputFormat.getOutputPath(job);
            DataOutputStream fileOut = fs.create(new Path(dir, name), progress);

            return new LineRecordWriter(fileOut);
        }
    }

    public static class CrawlDbStatMapper implements Mapper<Text, CrawlDatum, Text, LongWritable>
    {

        LongWritable COUNT_1 = new LongWritable(1);
        private boolean sort = false;

        @Override
        public void configure(JobConf job)
        {
            sort = job.getBoolean("db.reader.stats.sort", false);
        }

        @Override
        public void close()
        {
        }

        @Override
        public void map(Text key, CrawlDatum value, OutputCollector<Text, LongWritable> output, Reporter reporter) throws IOException
        {
            output.collect(new Text("T"), COUNT_1);
            output.collect(new Text("status " + value.getStatus()), COUNT_1);
            output.collect(new Text("retry " + value.getRetriesSinceFetch()), COUNT_1);
            output.collect(new Text("s"), new LongWritable((long) (value.getScore() * 1000.0)));

            if (sort)
            {
                URL u = new URL(key.toString());
                String host = u.getHost();
                output.collect(new Text("status " + value.getStatus() + " " + host), COUNT_1);
            }
        }
    }

    public static class CrawlDbStatCombiner implements Reducer<Text, LongWritable, Text, LongWritable>
    {

        LongWritable val = new LongWritable();

        public CrawlDbStatCombiner()
        {
        }

        @Override
        public void configure(JobConf job)
        {
        }

        @Override
        public void close()
        {
        }

        @Override
        public void reduce(Text key, Iterator<LongWritable> values, OutputCollector<Text, LongWritable> output, Reporter reporter) throws IOException
        {
            val.set(0L);
            String k = (key).toString();

            if (!k.equals("s"))
            {
                while (values.hasNext())
                {
                    LongWritable cnt = values.next();
                    val.set(val.get() + cnt.get());
                }

                output.collect(key, val);
            }
            else
            {
                long total = 0;
                long min = Long.MAX_VALUE;
                long max = Long.MIN_VALUE;

                while (values.hasNext())
                {
                    LongWritable cnt = values.next();

                    if (cnt.get() < min)
                    {
                        min = cnt.get();
                    }

                    if (cnt.get() > max)
                    {
                        max = cnt.get();
                    }

                    total += cnt.get();
                }

                output.collect(new Text("scn"), new LongWritable(min));
                output.collect(new Text("scx"), new LongWritable(max));
                output.collect(new Text("sct"), new LongWritable(total));
            }
        }
    }

    public static class CrawlDbStatReducer implements Reducer<Text, LongWritable, Text, LongWritable>
    {

        @Override
        public void configure(JobConf job)
        {
        }

        @Override
        public void close()
        {
        }

        @Override
        public void reduce(Text key, Iterator<LongWritable> values, OutputCollector<Text, LongWritable> output, Reporter reporter) throws IOException
        {
            String k = (key).toString();
            if (k.equals("T"))
            {
                // sum all values for this key
                long sum = 0;

                while (values.hasNext())
                {
                    sum += (values.next()).get();
                }

                // output sum
                output.collect(key, new LongWritable(sum));
            }
            else if (k.startsWith("status") || k.startsWith("retry"))
            {
                LongWritable cnt = new LongWritable();

                while (values.hasNext())
                {
                    LongWritable val = values.next();
                    cnt.set(cnt.get() + val.get());
                }

                output.collect(key, cnt);
            }
            else if (k.equals("scx"))
            {
                LongWritable cnt = new LongWritable(Long.MIN_VALUE);

                while (values.hasNext())
                {
                    LongWritable val = values.next();

                    if (cnt.get() < val.get())
                    {
                        cnt.set(val.get());
                    }
                }

                output.collect(key, cnt);
            }
            else if (k.equals("scn"))
            {
                LongWritable cnt = new LongWritable(Long.MAX_VALUE);

                while (values.hasNext())
                {
                    LongWritable val = values.next();

                    if (cnt.get() > val.get())
                    {
                        cnt.set(val.get());
                    }
                }

                output.collect(key, cnt);
            }
            else if (k.equals("sct"))
            {
                LongWritable cnt = new LongWritable();

                while (values.hasNext())
                {
                    LongWritable val = values.next();
                    cnt.set(cnt.get() + val.get());
                }

                output.collect(key, cnt);
            }
        }
    }

    public static class CrawlDbTopNMapper implements Mapper<Text, CrawlDatum, FloatWritable, Text>
    {

        private static final FloatWritable fw = new FloatWritable();
        private float min = 0.0f;

        @Override
        public void configure(JobConf job)
        {
            long lmin = job.getLong("db.reader.topn.min", 0);

            if (lmin != 0)
            {
                min = (float) lmin / 1000000.0f;
            }
        }

        @Override
        public void close()
        {
        }

        @Override
        public void map(Text key, CrawlDatum value, OutputCollector<FloatWritable, Text> output, Reporter reporter) throws IOException
        {
            if (value.getScore() < min)
            {
                return; // Don't collect low-scoring records.
            }

            if (value.getStatus() != CrawlDatum.STATUS_DB_FETCHED)
            {
                return; // Only collect fetched records.
            }

            fw.set(-value.getScore()); // reverse sorting order
            output.collect(fw, key); // invert mapping: score -> url
        }
    }

    public static class CrawlDbTopNReducer implements Reducer<FloatWritable, Text, FloatWritable, Text>
    {

        private long topN;
        private long count = 0L;

        @Override
        public void reduce(FloatWritable key, Iterator<Text> values, OutputCollector<FloatWritable, Text> output, Reporter reporter) throws IOException
        {
            while (values.hasNext() && count < topN)
            {
                key.set(Math.abs(key.get()));
                output.collect(key, values.next());
                count++;
            }
        }

        @Override
        public void configure(JobConf job)
        {
            topN = job.getLong("db.reader.topn", 100) / job.getNumReduceTasks();
        }

        @Override
        public void close()
        {
        }
    }

    @Override
    public void close()
    {
        closeReaders();
    }

    /**
     * Launch a job to count the URLs in DBase. This job is for statictics only.
     * It allows the user to know exactly the amount of URLs in DBase, and also
     * to know how many pages have been fetched or unfetched, etc.
     *
     * @param crawlDb The path to the DBase.
     * @param config  The configuration file.
     * @param sort    If we should sort or not.
     *
     * @return A Text object containing the formatted presentation.
     *
     * @throws IOException
     */
    public Text processStatJob(String crawlDb, Configuration config, boolean sort) throws IOException
    {
        Path tmpFolder = new Path(crawlDb, "stat_tmp" + System.currentTimeMillis());
        // Create job
        JobConf job = new AIMEJob(config);
        // Configure
        job.setJobName("MainDBaseStats");
        job.setBoolean("db.reader.stats.sort", sort);
        job.setInputFormat(SequenceFileInputFormat.class);
        job.setMapperClass(CrawlDbStatMapper.class);
        job.setCombinerClass(CrawlDbStatCombiner.class);
        job.setReducerClass(CrawlDbStatReducer.class);
        job.setOutputFormat(SequenceFileOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);
        // IO paths
        FileInputFormat.addInputPath(job, new Path(crawlDb, CrawlDB.CURRENT_NAME));
        FileOutputFormat.setOutputPath(job, tmpFolder);
        // Run job!
        JobClient.runJob(job);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("MapReduce job named: " + job.getJobName() + " completed!");
        }

        // Reading the result
        FileSystem fileSystem = FileSystem.get(config);
        SequenceFile.Reader[] rdrs = SequenceFileOutputFormat.getReaders(config, tmpFolder);
        Text key = new Text();
        LongWritable value = new LongWritable();
        TreeMap<String, LongWritable> stats = new TreeMap<>();
        for (SequenceFile.Reader reader : rdrs)
        {
            while (reader.next(key, value))
            {
                String k = key.toString();
                LongWritable val = stats.get(k);
                if (val == null)
                {
                    val = new LongWritable();
                    if (k.equals("scx"))
                    {
                        val.set(Long.MIN_VALUE);
                    }
                    if (k.equals("scn"))
                    {
                        val.set(Long.MAX_VALUE);
                    }
                    stats.put(k, val);
                }
                switch (k)
                {
                    case "scx":
                        if (val.get() < value.get())
                        {
                            val.set(value.get());
                        }
                        break;
                    case "scn":
                        if (val.get() > value.get())
                        {
                            val.set(value.get());
                        }
                        break;
                    default:
                        val.set(val.get() + value.get());
                        break;
                }
            }
            reader.close();
        }

        // Build the response.
        StringBuilder res = new StringBuilder();
        res.append("<html>");
        res.append("<head>");
        res.append(HtmlMessageBuilder.mainCSS());
        res.append("</head>");
        res.append("<body>");
        res.append("<div id=\"container\">");
        res.append("<table>");
        res.append("<tr>");

        LongWritable totalCnt = stats.get("T");
        stats.remove("T");

        res.append("<th align=\"center\">TOTAL URL: ").append(totalCnt.get()).append("</th>");
        res.append("</tr>");
        res.append("<tr>");
        res.append("<td align=\"left\">");
        res.append("<ul>");

        for (Map.Entry<String, LongWritable> entry : stats.entrySet())
        {
            String k = entry.getKey();
            LongWritable val = entry.getValue();
            if (k.equals("scn"))
            {
                res.append("<li><span class=\"subtitle\">Min. Score:</span> ").append(val.get() / 1000.0f).append("</li>");
            }
            else if (k.equals("scx"))
            {
                res.append("<li><span class=\"subtitle\">Max. Score:</span> ").append(val.get() / 1000.0f).append("</li>");
            }
            else if (k.equals("sct"))
            {
                res.append("<li><span class=\"subtitle\">Avg. Score:</span> ").append((float) ((((double) val.get()) / totalCnt.get()) / 1000.0)).append("</li>");
            }
            else if (k.startsWith("status"))
            {
                String[] st = k.split(" ");
                int code = Integer.parseInt(st[1]);

                if (st.length > 2)
                {
                    res.append("<li class=\"domain\">+ ").append(st[2]).append(" : ").append(val).append("</li>");
                }
                else
                {
                    res.append("<li><span class=\"subtitle\">").append(st[0].toUpperCase()).append(" ").append(code).append(" (").append(CrawlDatum.getStatusName((byte) code)).append(")</span> : ").append(val).append("</li>");
                }
            }
            else
            {
                res.append("<li><span class=\"subtitle\">").append(k.toUpperCase()).append("</span> : ").append(val).append("</li>");
            }
        }

        res.append("</ul>");
        res.append("</td>");
        res.append("</tr>");
        res.append("</table>");
        res.append("</div>");
        res.append("</body>");
        res.append("</html>");

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Cleaning MapReduce job named: " + job.getJobName());
        }

        fileSystem.delete(tmpFolder, true); // Remove tmp folder

        return new Text(res.toString());
    }

    public CrawlDatum get(String crawlDb, String url, Configuration config) throws IOException
    {
        Text key = new Text(url);
        CrawlDatum val = new CrawlDatum();
        openReaders(crawlDb, config);
        CrawlDatum res = (CrawlDatum) MapFileOutputFormat.getEntry(readers, new HashPartitioner<Text, CrawlDatum>(), key, val);

        return res;
    }

    public Text readUrl(String crawlDb, String url, Configuration config) throws IOException
    {
        StringBuilder res = new StringBuilder();

        // The MainDBase stores the URLs in the following format: http://aime.io/
        url = url.trim(); // Remove whitespace.

        CrawlDatum entry = get(crawlDb, url, config);

        res.append("<html>");
        res.append("<head>");
        res.append(HtmlMessageBuilder.mainCSS());
        res.append("</head>");
        res.append("<body>");
        res.append("<div id=\"container\">");
        res.append("<table>");
        res.append("<tr>");
        res.append("<th align=\"center\">").append(GeneralUtilities.breakString(url, 90, "<br/>")).append("</th>");
        res.append("</tr>");
        res.append("<tr>");
        res.append("<td align=\"left\">");
        res.append("<ul>");

        if (entry != null)
        {
            res.append(entry.toHtml());
        }
        else
        {
            res.append("<li class=\"subtitle\">URL not found!</li>");
        }

        res.append("</ul>");
        res.append("</td>");
        res.append("</tr>");
        res.append("</table>");
        res.append("</div>");
        res.append("</body>");
        res.append("</html>");

        return new Text(res.toString());
    }

    /**
     * Dumps the entire MainDBase to a file.
     *
     * @param crawlDb Path to DBase.
     * @param output  Path to output folder.
     * @param config  The configurations file.
     * @param format  The format used for the output. Can be: plain text or CSV.
     *
     * @throws IOException
     */
    public void processDumpJob(String crawlDb, String output, Configuration config, int format) throws IOException
    {
        Path outFolder = new Path(output);
        // Create job
        JobConf job = new AIMEJob(config);
        // Configure
        job.setJobName("Main_DBase_Dump");
        job.setInputFormat(SequenceFileInputFormat.class);
        if (format == CSV_FORMAT)
        {
            job.setOutputFormat(CrawlDatumCsvOutputFormat.class);
        }
        else
        {
            job.setOutputFormat(TextOutputFormat.class);
        }
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(CrawlDatum.class);
        // IO paths
        FileInputFormat.addInputPath(job, new Path(crawlDb, CrawlDB.CURRENT_NAME));
        FileOutputFormat.setOutputPath(job, outFolder);
        // Run
        JobClient.runJob(job);
    }

    /**
     * Returns the Top N ranked documents in the Main DBase.
     *
     * @param crawlDb Path to DBase.
     * @param topN    How many documents to fetch/show.
     * @param min     Minimum score. Score below this will not get showed.
     * @param config  The configurations file.
     *
     * @return A Text file containing the HTML presentation of the Top N ranked
     *         documents.
     *
     * @throws IOException
     */
    public Text processTopNJob(String crawlDb, long topN, float min, Configuration config) throws IOException
    {
        Path outFolder = new Path(config.get("mapred.temp.dir", ".") + "/readdb-topN-out-" + Integer.toString(new Random().nextInt(Integer.MAX_VALUE)));
        Path tempDir = new Path(config.get("mapred.temp.dir", ".") + "/readdb-topN-temp-" + Integer.toString(new Random().nextInt(Integer.MAX_VALUE)));
        // Create job
        JobConf job = new AIMEJob(config);
        // Configure
        job.setJobName("Main_DBase_TopN_Prepare");
        job.setInputFormat(SequenceFileInputFormat.class);
        job.setMapperClass(CrawlDbTopNMapper.class);
        job.setReducerClass(IdentityReducer.class);
        job.setOutputFormat(SequenceFileOutputFormat.class);
        job.setOutputKeyClass(FloatWritable.class);
        job.setOutputValueClass(Text.class);
        job.setLong("db.reader.topn.min", Math.round(1000000.0 * min));
        // IO paths
        FileInputFormat.addInputPath(job, new Path(crawlDb, CrawlDB.CURRENT_NAME));
        FileOutputFormat.setOutputPath(job, tempDir);
        // Run job!
        JobClient.runJob(job);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("MapReduce job named: " + job.getJobName() + " completed!");
        }

        // Create job
        job = new AIMEJob(config);
        // Configure
        job.setJobName("Main_DBase_TopN_Collect");
        job.setLong("db.reader.topn", topN);
        job.setInputFormat(SequenceFileInputFormat.class);
        job.setMapperClass(IdentityMapper.class);
        job.setReducerClass(CrawlDbTopNReducer.class);
        job.setOutputFormat(SequenceFileOutputFormat.class);
        job.setOutputKeyClass(FloatWritable.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(1); // create a single file.
        // IO paths
        FileInputFormat.addInputPath(job, tempDir);
        FileOutputFormat.setOutputPath(job, outFolder);
        // Run job!
        JobClient.runJob(job);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("MapReduce job named: " + job.getJobName() + " completed!");
        }

        // Reading the result
        SequenceFile.Reader[] rdrs = SequenceFileOutputFormat.getReaders(config, outFolder);
        FloatWritable key = new FloatWritable();
        Text value = new Text();
        LinkedHashMap<String, Float> stats = new LinkedHashMap<>();
        for (SequenceFile.Reader reader : rdrs)
        {
            while (reader.next(key, value))
            {
                stats.put(value.toString(), key.get());
            }
            reader.close();
        }

        StringBuilder res = new StringBuilder();
        res.append("<html>");
        res.append("<head>");
        res.append(HtmlMessageBuilder.mainCSS());
        res.append("</head>");
        res.append("<body>");
        res.append("<div id=\"container\">");
        res.append("<table>");
        res.append("<tr>");
        res.append("<th align=\"center\" colspan=\"3\">Showing Top \"").append(stats.size()).append("\" URLs:</th>");
        res.append("</tr>");

        int counter = 1;
        for (Map.Entry<String, Float> entry : stats.entrySet())
        {
            res.append("<tr>");
            res.append("<td align=\"center\"><b>#").append(counter).append("</b></td>");
            res.append("<td align=\"center\"><span class=\"subtitle\"><b>").append(entry.getValue()).append("</b></span></td>");
            res.append("<td align=\"left\"><a href=\"").append(entry.getKey()).append("\">").append(GeneralUtilities.breakString(entry.getKey(), 90, "<br/>")).append("</a></td>");
            res.append("</tr>");
            counter++;
        }

        res.append("</table>");
        res.append("</div>");
        res.append("</body>");
        res.append("</html>");

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Cleaning MapReduce job named: " + job.getJobName());
        }

        // Remove tmp folder
        FileSystem fs = FileSystem.get(config);
        fs.delete(tempDir, true);
        fs.delete(outFolder, true);

        return new Text(res.toString());
    }
}
