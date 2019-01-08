package io.aime.plugins.languageidentifier;

import io.aime.crawl.CrawlDatum;
import io.aime.crawl.Inlinks;
import io.aime.indexer.AIMEDocument;
import io.aime.indexer.IndexingException;
import io.aime.indexer.IndexingFilter;
import io.aime.metadata.DocMetadata;
import io.aime.net.protocols.Response;
import io.aime.parse.Parse;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;

/**
 * Adds
 * <code>lang</code> (language) field to the document.
 *
 * <p>
 * It tries to find the language of the document by:
 * <ul>
 * <li>First, checking if {@link HTMLLanguageParser} add some language
 * information</li>
 * <li>Then, checking if a
 * <code>Content-Language</code> HTTP header can be found</li>
 * <li>Finaly by analyzing the document content.</li>
 * </ul>
 * </p>
 *
 * @author Sami Siren
 * @author Jerome Charron
 * @author K-Zen
 */
public class LanguageIndexingFilter implements IndexingFilter
{

    private Configuration conf;
    private LanguageIdentifier languageIdentifier;

    @Override
    public AIMEDocument filter(AIMEDocument doc, Parse parse, Text url, CrawlDatum datum, Inlinks inlinks) throws IndexingException
    {
        // check if LANGUAGE found, possibly put there by HTMLLanguageParser
        String lang = parse.getData().getParseMeta().get(DocMetadata.LANGUAGE);

        // check if HTTP-header tels us the language
        if (lang == null)
        {
            lang = parse.getData().getContentMeta().get(Response.CONTENT_LANGUAGE);
        }

        if (lang == null)
        {
            StringBuilder text = new StringBuilder();
            /*
             * String[] anchors = fo.getAnchors(); for (int i = 0; i <
             * anchors.length; i++) { text+=anchors[i] + " "; }
             */
            text.append(parse.getData().getTitle()).append(" ").append(parse.getText());
            lang = this.languageIdentifier.identify(text);
        }

        if (lang == null || lang.isEmpty())
        {
            lang = "unknown";
        }

        doc.add("lang", lang);

        return doc;
    }

    @Override
    public void addIndexBackendOptions(Configuration conf)
    {
        // InternalTools.addFieldOptions("lang", IndexServerImplementation.STORE.YES, IndexServerImplementation.INDEX.UNTOKENIZED, conf);
    }

    @Override
    public void setConf(Configuration conf)
    {
        this.conf = conf;
        this.languageIdentifier = new LanguageIdentifier(conf);
    }

    @Override
    public Configuration getConf()
    {
        return this.conf;
    }

    @Override
    public void close()
    {
        this.languageIdentifier = null;
    }
}
