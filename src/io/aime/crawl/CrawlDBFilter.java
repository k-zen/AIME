package io.aime.crawl;

import io.aime.brain.Brain;
import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.MetadataGeneral;
import io.aime.brain.xml.Handler;
import io.aime.net.URLFilter;
import io.aime.net.URLFilters;
import io.aime.net.URLNormalizers;
import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.log4j.Logger;

/**
 * This class provides a way to separate the URL normalization and filtering
 * steps from the rest of CrawlDb manipulation code.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class CrawlDBFilter implements Mapper<Text, CrawlDatum, Text, CrawlDatum>
{

    private static final Logger LOG = Logger.getLogger(CrawlDBFilter.class.getName());
    public static final String URL_FILTERING = "crawldb.url.filters";
    public static final String URL_NORMALIZING = "crawldb.url.normalizers";
    public static final String URL_NORMALIZING_SCOPE = "crawldb.url.normalizers.scope";
    private boolean urlFiltering;
    private boolean urlNormalizers;
    private URLFilter[] filters;
    private URLNormalizers normalizers;
    private String scope;
    private Text newKey = new Text();

    @Override
    public void configure(JobConf job)
    {
        urlFiltering = job.getBoolean(URL_FILTERING, false);
        urlNormalizers = job.getBoolean(URL_NORMALIZING, false);

        if (urlFiltering) {
            filters = (URLFilter[]) Brain
                    .getClient(job)
                    .execute(Handler
                            .makeXMLRequest(BrainXMLData
                                    .newBuild()
                                    .setJob(BrainXMLData.JOB_REQUEST)
                                    .setClazz(MetadataGeneral.Data.class)
                                    .setFunction(MetadataGeneral.Data.FILTERS.getMethodName()))).get();
        }

        if (urlNormalizers) {
            scope = job.get(URL_NORMALIZING_SCOPE, URLNormalizers.SCOPE_CRAWLDB);
            normalizers = new URLNormalizers(job, scope);
        }
    }

    @Override
    public void close()
    {
    }

    @Override
    public void map(Text key, CrawlDatum value, OutputCollector<Text, CrawlDatum> output, Reporter reporter) throws IOException
    {
        String url = key.toString();

        if (urlNormalizers) {
            try {
                url = normalizers.normalize(url, scope); // normalize the url
            }
            catch (Exception e) {
                LOG.warn("Skipping: " + url + " : " + e.getMessage());
                url = null;
            }
        }

        if (url != null && urlFiltering) {
            try {
                url = URLFilters.filter(filters, url); // filter the url
            }
            catch (Exception e) {
                LOG.warn("Skipping: " + url + " : " + e.getMessage());
                url = null;
            }
        }

        if (url != null) { // if it passes
            newKey.set(url); // collect it
            output.collect(newKey, value);
        }
    }
}
