package io.aime.plugins.indexbasic;

import io.aime.crawl.CrawlDatum;
import io.aime.crawl.Inlinks;
import io.aime.indexer.AIMEDocument;
import io.aime.indexer.IndexingException;
import io.aime.indexer.IndexingFilter;
import io.aime.parse.Parse;
import io.aime.util.AIMEConstants;
import io.aime.util.Net;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

/**
 * Adds basic fields to the temporary index of AIME.
 *
 * <p>
 * This fields are the base point for a search index.</p>
 *
 * @author Nutch.org
 * @author K-Zen
 */
public class BasicIndexingFilter implements IndexingFilter
{

    private static final Logger LOG = Logger.getLogger(BasicIndexingFilter.class.getName());
    private int MAX_TITLE_LENGTH;
    private Configuration conf;

    @Override
    public AIMEDocument filter(AIMEDocument doc, Parse parse, Text url, CrawlDatum datum, Inlinks inlinks) throws IndexingException
    {
        Text reprUrl = (Text) datum.getMetadata().get(AIMEConstants.WRITABLE_REPR_URL_KEY.getTextConstant());
        String reprUrlString = reprUrl != null ? reprUrl.toString() : null;
        String urlString = url.toString();
        String host = null;
        String domain = null;

        // Page content
        doc.add("content", parse.getText());
        doc.add("contentraw", parse.getText());
        doc.add("contentfilter", parse.getText());

        // Title
        String title = parse.getData().getTitle();
        // Shorten the title if necessary.
        if (title.length() > MAX_TITLE_LENGTH)
        {
            title = title.substring(0, MAX_TITLE_LENGTH);
        }
        doc.add("title", title);

        // Resolv host, domain, site.
        try
        {
            URL u;
            if (reprUrlString != null)
            {
                u = new URL(reprUrlString);
                domain = Net.getDomainFromURL(reprUrlString);
            }
            else
            {
                u = new URL(urlString);
                domain = Net.getDomainFromURL(urlString);
            }

            host = u.getHost();
        }
        catch (MalformedURLException e)
        {
            LOG.error("Error breaking the URL into parts: " + urlString);
            throw new IndexingException(e);
        }

        if (host != null)
        {
            doc.add("host", host);
            doc.add("site", host);
            doc.add("domain", domain);
        }

        // Dates of the page.
        doc.add("fetchtime", String.valueOf(datum.getFetchTime()));
        doc.add("indextime", String.valueOf(System.currentTimeMillis()));

        // URL of the page.
        doc.add("url", urlString);

        return doc;
    }

    @Override
    public void addIndexBackendOptions(Configuration conf)
    {
        // InternalTools.addFieldOptions("content", IndexServerImplementation.STORE.YES, IndexServerImplementation.INDEX.TOKENIZED, conf);
        // InternalTools.addFieldOptions("contentraw", IndexServerImplementation.STORE.YES, IndexServerImplementation.INDEX.TOKENIZED, conf);
        // InternalTools.addFieldOptions("contentfilter", IndexServerImplementation.STORE.YES, IndexServerImplementation.INDEX.TOKENIZED, conf);
        // InternalTools.addFieldOptions("domain", IndexServerImplementation.STORE.YES, IndexServerImplementation.INDEX.UNTOKENIZED, conf);
        // InternalTools.addFieldOptions("fetchtime", IndexServerImplementation.STORE.YES, IndexServerImplementation.INDEX.NO, conf);
        // InternalTools.addFieldOptions("host", IndexServerImplementation.STORE.YES, IndexServerImplementation.INDEX.TOKENIZED, conf);
        // InternalTools.addFieldOptions("indextime", IndexServerImplementation.STORE.YES, IndexServerImplementation.INDEX.NO, conf);
        // InternalTools.addFieldOptions("site", IndexServerImplementation.STORE.YES, IndexServerImplementation.INDEX.UNTOKENIZED, conf);
        // InternalTools.addFieldOptions("title", IndexServerImplementation.STORE.YES, IndexServerImplementation.INDEX.TOKENIZED, conf);
        // InternalTools.addFieldOptions("url", IndexServerImplementation.STORE.YES, IndexServerImplementation.INDEX.TOKENIZED, conf);
    }

    @Override
    public void setConf(Configuration conf)
    {
        this.conf = conf;
        this.MAX_TITLE_LENGTH = conf.getInt("indexer.max.title.length", 100);
    }

    @Override
    public Configuration getConf()
    {
        return this.conf;
    }

    @Override
    public void close()
    {
        // Ignore.
    }
}
