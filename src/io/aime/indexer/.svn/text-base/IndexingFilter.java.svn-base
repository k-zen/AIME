package io.aime.indexer;

// Apache Hadoop
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;

// AIME
import io.aime.parse.Parse;
import io.aime.crawl.CrawlDatum;
import io.aime.crawl.Inlinks;
import io.aime.plugin.Pluggable;

/**
 * Extension point for indexing. Permits one to add metadata to the indexed
 * fields. All plugins found which implement this extension point are run
 * sequentially on the parse.
 */
public interface IndexingFilter extends Pluggable, Configurable {

    /**
     * The name of the extension point.
     */
    final static String X_POINT_ID = IndexingFilter.class.getName();

    /**
     * Adds fields or otherwise modifies the document that will be indexed for a
     * parse. Unwanted documents can be removed from indexing by returning a
     * null value.
     *
     * @param doc     document instance for collecting fields
     * @param parse   parse data instance
     * @param url     page url
     * @param datum   crawl datum for the page
     * @param inlinks page inlinks
     *
     * @return modified (or a new) document instance, or null (meaning the
     *         document should be discarded)
     *
     * @throws IndexingException
     */
    AIMEDocument filter(AIMEDocument doc, Parse parse, Text url, CrawlDatum datum, Inlinks inlinks) throws IndexingException;

    /**
     * Release all objects hold into memory.
     */
    void close();

    /**
     * Adds index-level configuraition options. Implementations can update given
     * configuration to pass document-independent information to indexing
     * backends. As a rule of thumb, prefix meta keys with the name of the
     * backend intended. For example, when passing information to lucene
     * backend, prefix keys with "lucene.".
     *
     * @param conf Configuration instance.
     */
    public void addIndexBackendOptions(Configuration conf);
}
