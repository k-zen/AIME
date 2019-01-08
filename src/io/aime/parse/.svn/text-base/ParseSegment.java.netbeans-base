package io.aime.parse;

import io.aime.aimemisc.digest.SignatureFactory;
import io.aime.crawl.CrawlDatum;
import io.aime.protocol.Content;
import io.aime.util.AIMEConfiguration;
import io.aime.util.AIMEConstants;
import io.aime.util.AIMEJob;
import io.aime.util.ProcessKiller;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.SwingWorker;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.UTF8;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

public class ParseSegment extends Configured implements Tool, Mapper<WritableComparable, Content, Text, ParseImplementation>, Reducer<Text, Writable, Text, Writable>
{

    private static final Logger LOG = Logger.getLogger(ParseSegment.class.getName());
    private Text newKey = new Text();

    public ParseSegment()
    {
        this(null);
    }

    public ParseSegment(Configuration conf)
    {
        super(conf);
    }

    @Override
    public void configure(JobConf job)
    {
        setConf(job);
    }

    @Override
    public void close()
    {
    }

    @Override
    public void map(WritableComparable key, Content content, OutputCollector<Text, ParseImplementation> output, Reporter reporter) throws IOException
    {
        // convert on the fly from old UTF8 keys
        if (key instanceof UTF8)
        {
            newKey.set(key.toString());
            key = newKey;
        }

        int status = Integer.parseInt(content.getMetadata().get(AIMEConstants.FETCH_STATUS_KEY.getStringConstant()));
        if (status != CrawlDatum.STATUS_FETCH_SUCCESS)
        {
            // content not fetched successfully, skip document
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Skipping: " + key + " (content incomplete).");
            }

            return;
        }

        ParseResult parseResult;
        try
        {
            parseResult = new ParserRun(getConf()).getParseResult(content);
        }
        catch (ParseException e)
        {
            LOG.warn("Error parsing: " + key + ". Error: " + e.toString());
            return;
        }

        for (Entry<Text, Parse> entry : parseResult)
        {
            Text url = entry.getKey();
            Parse parse = entry.getValue();
            ParseStatus parseStatus = parse.getData().getStatus();
            reporter.incrCounter("ParserStatus", ParseStatus.majorCodes[parseStatus.getMajorCode()], 1);

            if (!parseStatus.isSuccess())
            {
                LOG.warn("Error parsing: " + key + ". Error: " + parseStatus);
                parse = parseStatus.getEmptyParse(getConf());
            }

            // Pass segment name to get_parse_result data
            parse.getData().getContentMeta().set(AIMEConstants.SEGMENT_NAME_KEY.getStringConstant(), getConf().get(AIMEConstants.SEGMENT_NAME_KEY.getStringConstant()));

            // Compute the new signature.
            // Always compute on the text, if there is no text, then on the content.
            long signature;
            if (parse.getText() != null && !parse.getText().isEmpty())
            { // Compute on text.
                signature = SignatureFactory.getSignature(getConf()).calculate(parse.getText());
            }
            else
            { // Compute on content.
                signature = SignatureFactory.getSignature(getConf()).calculateRawText(content.toString(), new URL(content.getUrl()));
            }
            parse.getData().getContentMeta().set(AIMEConstants.SIGNATURE_KEY.getStringConstant(), String.valueOf(signature));
            parse.getData().getContentMeta().set(AIMEConstants.SCORE_KEY.getStringConstant(), content.getMetadata().get(AIMEConstants.SCORE_KEY.getStringConstant()));

            output.collect(url, new ParseImplementation(new ParseText(parse.getText()), parse.getData(), parse.isCanonical()));
        }
    }

    @Override
    public void reduce(Text key, Iterator<Writable> values, OutputCollector<Text, Writable> output, Reporter reporter) throws IOException
    {
        output.collect(key, values.next()); // collect first value
    }

    public void parse(Path segment) throws IOException
    {
        if (LOG.isInfoEnabled())
        {
            LOG.info("Segment: " + segment);
        }

        // Create job
        JobConf job = new AIMEJob(getConf());
        // Configure
        job.setJobName(AIMEConstants.PARSE_JOB_NAME.getStringConstant());
        job.set(AIMEConstants.SEGMENT_NAME_KEY.getStringConstant(), segment.getName());
        job.setInputFormat(SequenceFileInputFormat.class);
        job.setMapperClass(ParseSegment.class);
        job.setReducerClass(ParseSegment.class);
        job.setOutputFormat(ParseOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(ParseImplementation.class);
        // IO paths
        FileInputFormat.addInputPath(job, new Path(segment, Content.DIR_NAME));
        FileOutputFormat.setOutputPath(job, segment);
        // Run
        JobClient.runJob(job);
    }

    public static int runProcess(String[] args, SwingWorker<?, ?> runner) throws Exception
    {
        Configuration conf = new AIMEConfiguration().create();
        return ProcessKiller.checkExitCode(
                ToolRunner.run(conf, new ParseSegment(), args),
                AIMEConstants.PARSE_JOB_NAME.getStringConstant(),
                runner,
                conf);
    }

    @Override
    public int run(String[] args) throws Exception
    {
        parse(new Path(args[0]));
        return 0;
    }
}
