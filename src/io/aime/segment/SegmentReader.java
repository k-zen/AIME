package io.aime.segment;

import io.aime.crawl.AIMEWritable;
import io.aime.crawl.CrawlDatum;
import io.aime.parse.ParseData;
import io.aime.parse.ParseText;
import io.aime.protocol.Content;
import io.aime.util.AIMEConstants;
import io.aime.util.AIMEJob;
import io.aime.util.HadoopFSUtil;
import io.aime.util.HtmlMessageBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.UTF8;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapFileOutputFormat;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.util.Progressable;
import org.apache.log4j.Logger;

/**
 * Dump the content of a segment.
 *
 * @author Nutch.org
 * @author K-Zen
 */
public class SegmentReader extends Configured implements Reducer<Text, AIMEWritable, Text, Text>
{

    private static final Logger LOG = Logger.getLogger(SegmentReader.class.getName());
    private static final SegmentReader _INSTANCE = new SegmentReader();
    private boolean co, fe, ge, pa, pd, pt;
    private FileSystem fs;
    private static final String[][] keys = new String[][]
    {
        {
            "co", "Content::\n"
        },
        {
            "ge", "Crawl Generate::\n"
        },
        {
            "fe", "Crawl Fetch::\n"
        },
        {
            "pa", "Crawl Parse::\n"
        },
        {
            "pd", "ParseData::\n"
        },
        {
            "pt", "ParseText::\n"
        }
    };
    long recNo = 0L;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private SegmentReader()
    {
        super(null);
    }

    private SegmentReader(Configuration conf, boolean co, boolean fe, boolean ge, boolean pa, boolean pd, boolean pt)
    {
        super(conf);
        this.co = co;
        this.fe = fe;
        this.ge = ge;
        this.pa = pa;
        this.pd = pd;
        this.pt = pt;

        try
        {
            this.fs = FileSystem.get(conf);
        }
        catch (IOException e)
        {
            LOG.warn(e.getMessage(), e);
        }
    }

    public static SegmentReader getInstance()
    {
        return _INSTANCE;
    }

    @Override
    public void configure(JobConf job)
    {
        this.setConf(job);
        this.co = this.getConf().getBoolean("segment.reader.co", true);
        this.fe = this.getConf().getBoolean("segment.reader.fe", true);
        this.ge = this.getConf().getBoolean("segment.reader.ge", true);
        this.pa = this.getConf().getBoolean("segment.reader.pa", true);
        this.pd = this.getConf().getBoolean("segment.reader.pd", true);
        this.pt = this.getConf().getBoolean("segment.reader.pt", true);

        try
        {
            this.fs = FileSystem.get(this.getConf());
        }
        catch (IOException e)
        {
            LOG.warn(e.getMessage(), e);
        }
    }

    @Override
    public void close()
    {
    }

    @Override
    public void reduce(Text key, Iterator<AIMEWritable> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException
    {
        StringBuilder dump = new StringBuilder();
        dump.append("\nRecno:: ").append(recNo++).append("\n");
        dump.append("URL:: ").append(key.toString()).append("\n");

        while (values.hasNext())
        {
            Writable value = values.next().get(); // unwrap
            if (value instanceof CrawlDatum)
            {
                dump.append("\nCrawlDatum::\n").append(((CrawlDatum) value).toString());
            }
            else if (value instanceof Content)
            {
                dump.append("\nContent::\n").append(((Content) value).toString());
            }
            else if (value instanceof ParseData)
            {
                dump.append("\nParseData::\n").append(((ParseData) value).toString());
            }
            else if (value instanceof ParseText)
            {
                dump.append("\nParseText::\n").append(((ParseText) value).toString());
            }
            else
            {
                LOG.warn("Non-recognized type: " + value.getClass());
            }
        }

        output.collect(key, new Text(dump.toString()));
    }

    private JobConf createJobConf()
    {
        JobConf job = new AIMEJob(this.getConf());
        job.setBoolean("segment.reader.co", this.co);
        job.setBoolean("segment.reader.fe", this.fe);
        job.setBoolean("segment.reader.ge", this.ge);
        job.setBoolean("segment.reader.pa", this.pa);
        job.setBoolean("segment.reader.pd", this.pd);
        job.setBoolean("segment.reader.pt", this.pt);

        return job;
    }

    public void dump(Path segment, Path output) throws IOException
    {
        JobConf job = this.createJobConf();
        job.setJobName("Segment_DBase_Dump");

        if (ge)
        {
            FileInputFormat.addInputPath(job, new Path(segment, CrawlDatum.GENERATE_DIR_NAME));
        }
        if (fe)
        {
            FileInputFormat.addInputPath(job, new Path(segment, CrawlDatum.FETCH_DIR_NAME));
        }
        if (pa)
        {
            FileInputFormat.addInputPath(job, new Path(segment, CrawlDatum.PARSE_DIR_NAME));
        }
        if (co)
        {
            FileInputFormat.addInputPath(job, new Path(segment, Content.DIR_NAME));
        }
        if (pd)
        {
            FileInputFormat.addInputPath(job, new Path(segment, ParseData.DIR_NAME));
        }
        if (pt)
        {
            FileInputFormat.addInputPath(job, new Path(segment, ParseText.DIR_NAME));
        }

        job.setInputFormat(SequenceFileInputFormat.class);
        job.setMapperClass(InputCompatMapper.class);
        job.setReducerClass(SegmentReader.class);
        Path tempDir = new Path(job.get("hadoop.tmp.dir", "/tmp") + "/segread-" + new java.util.Random().nextInt());
        fs.delete(tempDir, true);
        FileOutputFormat.setOutputPath(job, tempDir);
        job.setOutputFormat(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(AIMEWritable.class);
        JobClient.runJob(job);
        // concatenate the output
        Path dumpFile = new Path(output, job.get("segment.dump.dir", "dump"));
        // remove the old file
        fs.delete(dumpFile, true);
        FileStatus[] fstats = fs.listStatus(tempDir, HadoopFSUtil.getPassAllFilter());
        Path[] files = HadoopFSUtil.getPaths(fstats);

        PrintWriter writer = null;
        int currentRecordNumber = 0;
        if (files.length > 0)
        {
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fs.create(dumpFile))));

            try
            {
                for (int i = 0; i < files.length; i++)
                {
                    Path partFile = files[i];
                    try
                    {
                        currentRecordNumber = append(fs, job, partFile, writer, currentRecordNumber);
                    }
                    catch (IOException exception)
                    {
                        LOG.warn("Impossible to copy content from: " + partFile.toString() + " to " + dumpFile.toString());
                        LOG.warn(exception.getMessage());
                    }
                }
            }
            finally
            {
                writer.close();
            }
        }

        fs.delete(tempDir);
    }

    /**
     * Appends two files and updates the Recno counter
     */
    private int append(FileSystem fs, Configuration conf, Path src, PrintWriter writer, int currentRecordNumber) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(src)));

        try
        {
            String line = reader.readLine();

            while (line != null)
            {
                if (line.startsWith("Recno:: "))
                {
                    line = "Recno:: " + currentRecordNumber++;
                }

                writer.println(line);
                line = reader.readLine();
            }

            return currentRecordNumber;
        }
        finally
        {
            reader.close();
        }
    }

    /**
     * This method iterates through all the available segments looking for the
     * specified URL/Key/Document and returns all information contained for that
     * specific record.
     *
     * @param key  The key of the document.
     * @param conf The configuration file.
     *
     * @return An HTML table with all info for the document.
     *
     * @throws IOException
     */
    public Text get(final Text key, final Configuration conf) throws IOException
    {
        final Map<String, List<Writable>> results = new LinkedHashMap<String, List<Writable>>();
        List<Path> segments = new ArrayList<Path>();

        /*
         * List the segments.
         */
        this.fs = FileSystem.get(conf);
        FileStatus[] fstats = this.fs.listStatus(new Path(AIMEConstants.SEGMENTDBASE_PATH.getStringConstant()), HadoopFSUtil.getPassDirectoriesFilter(this.fs));

        /*
         * Get all the segment paths.
         */
        Path[] files = HadoopFSUtil.getPaths(fstats);
        segments.addAll(Arrays.asList(files));

        StringBuilder res = new StringBuilder();
        res.append("<html>");
        res.append("<head>");
        res.append(HtmlMessageBuilder.mainCSS());
        res.append("</head>");
        res.append("<body>");
        res.append("<div id=\"container\">");
        res.append("<table>");

        for (int i = 0; i < segments.size(); i++)
        {
            ArrayList<Thread> threads = new ArrayList<Thread>();
            final Path dir = segments.get(i);

            threads.add(new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        List<Writable> res = SegmentReader.this.getMapRecords(new Path(dir, Content.DIR_NAME), key, conf);
                        results.put("co", res);
                    }
                    catch (Exception e)
                    {
                        LOG.warn(e.getMessage(), e);
                    }
                }
            });
            threads.add(new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        List<Writable> res = SegmentReader.this.getMapRecords(new Path(dir, CrawlDatum.FETCH_DIR_NAME), key, conf);
                        results.put("fe", res);
                    }
                    catch (Exception e)
                    {
                        LOG.warn(e.getMessage(), e);
                    }
                }
            });
            threads.add(new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        List<Writable> res = SegmentReader.this.getSeqRecords(new Path(dir, CrawlDatum.GENERATE_DIR_NAME), key, conf);
                        results.put("ge", res);
                    }
                    catch (Exception e)
                    {
                        LOG.warn(e.getMessage(), e);
                    }
                }
            });
            threads.add(new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        List<Writable> res = SegmentReader.this.getSeqRecords(new Path(dir, CrawlDatum.PARSE_DIR_NAME), key, conf);
                        results.put("pa", res);
                    }
                    catch (Exception e)
                    {
                        LOG.warn(e.getMessage(), e);
                    }
                }
            });
            threads.add(new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        List<Writable> res = SegmentReader.this.getMapRecords(new Path(dir, ParseData.DIR_NAME), key, conf);
                        results.put("pd", res);
                    }
                    catch (Exception e)
                    {
                        LOG.warn(e.getMessage(), e);
                    }
                }
            });
            threads.add(new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        List<Writable> res = SegmentReader.this.getMapRecords(new Path(dir, ParseText.DIR_NAME), key, conf);
                        results.put("pt", res);
                    }
                    catch (Exception e)
                    {
                        LOG.warn(e.getMessage(), e);
                    }
                }
            });

            Iterator<Thread> it = threads.iterator();
            while (it.hasNext())
            {
                it.next().start();
            }

            int cnt;
            do
            {
                cnt = 0;
                try
                {
                    Thread.sleep(5000);
                }
                catch (Exception e)
                {
                }

                it = threads.iterator();
                while (it.hasNext())
                {
                    if (it.next().isAlive())
                    {
                        cnt++;
                    }
                }

                if ((cnt > 0) && (true))
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("[" + cnt + "]: to search.");
                    }
                }
            } while (cnt > 0);

            for (int j = 0; j < keys.length; j++)
            {
                List<Writable> resData = results.get(keys[j][0]);

                if (resData != null && resData.size() > 0)
                {
                    for (int k = 0; k < resData.size(); k++)
                    {
                        res.append("<tr>");
                        res.append("<th align=\"center\">").append(keys[j][1]).append("</td>");
                        res.append("<td align=\"left\">").append(StringEscapeUtils.escapeXml(resData.get(k).toString()).replaceAll("\n", "<br/>")).append("</td>");
                        res.append("</tr>");
                    }
                }
            }

            /*
             * Interrupt all threads.
             */
            it = threads.iterator();
            while (it.hasNext())
            {
                it.next().interrupt();
            }
        }

        res.append("</table>");
        res.append("</div>");
        res.append("</body>");
        res.append("</html>");

        return new Text(res.toString());
    }

    private List<Writable> getMapRecords(Path dir, Text key, Configuration conf) throws Exception
    {
        MapFile.Reader[] readers = MapFileOutputFormat.getReaders(fs, dir, conf);
        ArrayList<Writable> res = new ArrayList<Writable>();
        Class keyClass = readers[0].getKeyClass();
        Class valueClass = readers[0].getValueClass();

        if (!keyClass.getName().equals("org.apache.hadoop.io.Text"))
        {
            throw new IOException("Incompatible key: " + keyClass.getName());
        }

        Writable value = (Writable) valueClass.newInstance();

        // we don't know the partitioning schema
        for (int i = 0; i < readers.length; i++)
        {
            if (readers[i].get(key, value) != null)
            {
                res.add(value);
            }

            readers[i].close();
        }

        return res;
    }

    private List<Writable> getSeqRecords(Path dir, Text key, Configuration conf) throws Exception
    {
        SequenceFile.Reader[] readers = SequenceFileOutputFormat.getReaders(conf, dir);
        ArrayList<Writable> res = new ArrayList<Writable>();
        Class keyClass = readers[0].getKeyClass();
        Class valueClass = readers[0].getValueClass();

        if (!keyClass.getName().equals("org.apache.hadoop.io.Text"))
        {
            throw new IOException("Incompatible key: " + keyClass.getName());
        }

        Writable aKey = (Writable) keyClass.newInstance();
        Writable value = (Writable) valueClass.newInstance();

        for (int i = 0; i < readers.length; i++)
        {
            while (readers[i].next(aKey, value))
            {
                if (aKey.equals(key))
                {
                    res.add(value);
                }
            }

            readers[i].close();
        }

        return res;
    }

    /**
     * This method returns an HTML table with stats about all segments.
     *
     * @param conf The configuration file.
     *
     * @return An HTML table with info regarding all segments.
     *
     * @throws IOException
     */
    public Text list(Configuration conf) throws IOException
    {
        List<Path> segments = new ArrayList<Path>();

        /*
         * List the segments.
         */
        this.fs = FileSystem.get(conf);
        FileStatus[] fstats = this.fs.listStatus(new Path(AIMEConstants.SEGMENTDBASE_PATH.getStringConstant()), HadoopFSUtil.getPassDirectoriesFilter(this.fs));

        /*
         * Get all the segment paths.
         */
        Path[] files = HadoopFSUtil.getPaths(fstats);
        segments.addAll(Arrays.asList(files));

        StringBuilder res = new StringBuilder();
        res.append("<html>");
        res.append("<head>");
        res.append(HtmlMessageBuilder.mainCSS());
        res.append("</head>");
        res.append("<body>");
        res.append("<div id=\"container\">");
        res.append("<table>");
        res.append("<tr>");
        res.append("<th align=\"center\" colspan=\"6\">Segments Informations:</th>");
        res.append("</tr>");
        res.append("<tr>");
        res.append("<th align=\"center\">NAME</th>");
        res.append("<th align=\"center\">GENERATED</th>");
        res.append("<th align=\"center\">FETCHER START</th>");
        res.append("<th align=\"center\">FETCHER END</th>");
        res.append("<th align=\"center\">FETCHED</th>");
        res.append("<th align=\"center\">PARSED</th>");
        res.append("</tr>");

        for (int i = 0; i < segments.size(); i++)
        {
            Path dir = segments.get(i);
            SegmentReaderStats stats = new SegmentReaderStats();

            /*
             * Retrieve the stats.
             */
            this.getStats(dir, stats, conf);

            res.append("<tr>");
            res.append("<td align=\"center\">").append(dir.getName()).append("</td>");

            if (stats.generated == -1)
            {
                res.append("<td align=\"center\">Unknown Documents</td>");
            }
            else
            {
                res.append("<td align=\"center\">").append(stats.generated).append(" Documents</td>");
            }

            if (stats.start == -1)
            {
                res.append("<td align=\"center\">Unknown Date</td>");
            }
            else
            {
                res.append("<td align=\"center\">").append(sdf.format(new Date(stats.start))).append("</td>");
            }

            if (stats.end == -1)
            {
                res.append("<td align=\"center\">Unknown Date</td>");
            }
            else
            {
                res.append("<td align=\"center\">").append(sdf.format(new Date(stats.end))).append("</td>");
            }

            if (stats.fetched == -1)
            {
                res.append("<td align=\"center\">Unknown Documents</td>");
            }
            else
            {
                res.append("<td align=\"center\">").append(stats.fetched).append(" Documents</td>");
            }

            if (stats.parsed == -1)
            {
                res.append("<td align=\"center\">Unknown Documents</td>");
            }
            else
            {
                res.append("<td align=\"center\">").append(stats.parsed).append(" Documents</td>");
            }

            res.append("</tr>");
        }

        res.append("</table>");
        res.append("</div>");
        res.append("</body>");
        res.append("</html>");

        return new Text(res.toString());
    }

    public void getStats(Path segment, final SegmentReaderStats stats, Configuration conf) throws IOException
    {
        SequenceFile.Reader[] readers = SequenceFileOutputFormat.getReaders(conf, new Path(segment, CrawlDatum.GENERATE_DIR_NAME));
        long cnt = 0L;
        Text key = new Text();

        for (int i = 0; i < readers.length; i++)
        {
            while (readers[i].next(key))
            {
                cnt++;
            }

            readers[i].close();
        }

        stats.generated = cnt;
        Path fetchDir = new Path(segment, CrawlDatum.FETCH_DIR_NAME);

        if (fs.exists(fetchDir) && fs.getFileStatus(fetchDir).isDir())
        {
            cnt = 0L;
            long start = Long.MAX_VALUE;
            long end = Long.MIN_VALUE;
            CrawlDatum value = new CrawlDatum();
            MapFile.Reader[] mreaders = MapFileOutputFormat.getReaders(fs, fetchDir, conf);

            for (int i = 0; i < mreaders.length; i++)
            {
                while (mreaders[i].next(key, value))
                {
                    cnt++;
                    if (value.getFetchTime() < start)
                    {
                        start = value.getFetchTime();
                    }
                    if (value.getFetchTime() > end)
                    {
                        end = value.getFetchTime();
                    }
                }

                mreaders[i].close();
            }

            stats.start = start;
            stats.end = end;
            stats.fetched = cnt;
        }

        Path parseDir = new Path(segment, ParseData.DIR_NAME);
        if (fs.exists(fetchDir) && fs.getFileStatus(fetchDir).isDir())
        {
            cnt = 0L;
            long errors = 0L;
            ParseData value = new ParseData();
            MapFile.Reader[] mreaders = MapFileOutputFormat.getReaders(fs, parseDir, conf);

            for (int i = 0; i < mreaders.length; i++)
            {
                while (mreaders[i].next(key, value))
                {
                    cnt++;
                    if (!value.getStatus().isSuccess())
                    {
                        errors++;
                    }
                }

                mreaders[i].close();
            }

            stats.parsed = cnt;
            stats.parseErrors = errors;
        }
    }

    public static class InputCompatMapper extends MapReduceBase implements Mapper<WritableComparable, Writable, Text, AIMEWritable>
    {

        private Text newKey = new Text();

        @Override
        public void map(WritableComparable key, Writable value, OutputCollector<Text, AIMEWritable> collector, Reporter reporter) throws IOException
        {
            // convert on the fly from old formats with UTF8 keys
            if (key instanceof UTF8)
            {
                newKey.set(key.toString());
                key = newKey;
            }

            collector.collect((Text) key, new AIMEWritable(value));
        }
    }

    /**
     * Implements a text output format
     */
    public static class TextOutputFormat extends FileOutputFormat<WritableComparable, Writable>
    {

        @Override
        public RecordWriter<WritableComparable, Writable> getRecordWriter(final FileSystem fs, JobConf job, String name, final Progressable progress) throws IOException
        {
            final Path segmentDumpFile = new Path(FileOutputFormat.getOutputPath(job), name);

            // Get the old copy out of the way
            if (fs.exists(segmentDumpFile))
            {
                fs.delete(segmentDumpFile, true);
            }

            final PrintStream printStream = new PrintStream(fs.create(segmentDumpFile));
            return new RecordWriter<WritableComparable, Writable>()
            {
                @Override
                public synchronized void write(WritableComparable key, Writable value) throws IOException
                {
                    printStream.println(value);
                }

                @Override
                public synchronized void close(Reporter reporter) throws IOException
                {
                    printStream.close();
                }
            };
        }
    }

    public static class SegmentReaderStats
    {

        public long start = -1L;
        public long end = -1L;
        public long generated = -1L;
        public long fetched = -1L;
        public long fetchErrors = -1L;
        public long parsed = -1L;
        public long parseErrors = -1L;
    }
}
