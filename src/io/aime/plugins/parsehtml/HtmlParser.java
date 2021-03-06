package io.aime.plugins.parsehtml;

import io.aime.aimemisc.datamining.Block;
import io.aime.aimemisc.datamining.WebContentBlockDetection;
import io.aime.metadata.DocMetadata;
import io.aime.parse.HtmlMetaTags;
import io.aime.parse.HtmlParseFilters;
import io.aime.parse.Outlink;
import io.aime.parse.Parse;
import io.aime.parse.ParseData;
import io.aime.parse.ParseImplementation;
import io.aime.parse.ParseResult;
import io.aime.parse.ParseStatus;
import io.aime.parse.Parser;
import io.aime.protocol.Content;
import io.aime.util.AIMEConstants;
import io.aime.util.DOMBuilder;
import io.aime.util.DOMContentUtils;
import io.aime.util.DOMTreeEcho;
import io.aime.util.EncodingDetector;
import io.aime.util.GeneralUtilities;
import io.aime.util.HtmlMetaProcessor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hadoop.conf.Configuration;
import org.apache.html.dom.HTMLDocumentImpl;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class HtmlParser implements Parser
{

    private static final String KEY = HtmlParser.class.getName();
    private static final Logger LOG = Logger.getLogger(KEY);
    // I used 1000 bytes at first, but  found that some documents have 
    // meta tag well past the first 1000 bytes. 
    // (e.g. http://cn.promo.yahoo.com/customcare/music.html)
    private static final int CHUNK_SIZE = 2000;
    private static Pattern metaPattern = Pattern.compile("<meta\\s+([^>]*http-equiv=\"?content-type\"?[^>]*)>", Pattern.CASE_INSENSITIVE);
    private static Pattern charsetPattern = Pattern.compile("charset=\\s*([a-z][_\\-0-9a-z]*)", Pattern.CASE_INSENSITIVE);
    private Configuration conf;
    private DOMContentUtils utils;
    private HtmlParseFilters htmlParseFilters;
    private String cachingPolicy;
    private String defaultCharEncoding;

    @Override
    public void setConf(Configuration conf)
    {
        this.conf = conf;
        this.htmlParseFilters = new HtmlParseFilters(this.getConf());
        this.defaultCharEncoding = this.getConf().get("parser.character.encoding.default", "windows-1252");
        this.utils = new DOMContentUtils(conf);
        this.cachingPolicy = this.getConf().get("parser.caching.forbidden.policy", AIMEConstants.CACHING_FORBIDDEN_CONTENT.getStringConstant());
    }

    @Override
    public Configuration getConf()
    {
        return this.conf;
    }

    /**
     * Given a byte[] representing an html file of an <em>unknown</em> encoding,
     * read out 'charset' parameter in the meta tag from the first CHUNK_SIZE
     * bytes.
     *
     * <p>
     * If there's no meta tag for Content-Type or no charset is specified,
     * <code>null</code> is returned. FIXME: non-byte oriented character
     * encodings (UTF-16, UTF-32) can't be handled with this. We need to do
     * something similar to what's done by mozilla
     * (http://lxr.mozilla.org/seamonkey/source/parser/htmlparser/src/nsParser.cpp#1993).</p>
     *
     * <p>
     * See also http://www.w3.org/TR/REC-xml/#sec-guessing.</p>
     *
     * @param content The contents of the file in a byte array.
     *
     * @return The character encoding identifier.
     */
    protected static String sniffCharacterEncoding(byte[] content)
    {
        int length = content.length < CHUNK_SIZE ? content.length : CHUNK_SIZE;

        // We don't care about non-ASCII parts so that it's sufficient
        // to just inflate each byte to a 16-bit value by padding. 
        // For instance, the sequence {0x41, 0x82, 0xb7} will be turned into 
        // {U+0041, U+0082, U+00B7}. 
        String str;
        try
        {
            str = new String(content, 0, length, Charset.forName("ASCII").toString());
        }
        catch (UnsupportedEncodingException e)
        {
            // code should never come here, but just in case... 
            return null;
        }

        Matcher metaMatcher = metaPattern.matcher(str);
        String encoding = null;
        if (metaMatcher.find())
        {
            Matcher charsetMatcher = charsetPattern.matcher(metaMatcher.group(1));
            if (charsetMatcher.find())
            {
                encoding = charsetMatcher.group(1);
            }
        }

        return encoding;
    }

    @Override
    public ParseResult getParseResult(Content content)
    {
        HtmlMetaTags metaTags = new HtmlMetaTags();

        URL base;
        try
        {
            base = new URL(content.getBaseUrl());
        }
        catch (MalformedURLException e)
        {
            return new ParseStatus(e).getEmptyParseResult(content.getUrl(), getConf());
        }

        String text = new String();
        String title = new String();
        Block[] contentBlocks;
        Outlink[] outlinks = new Outlink[0];
        DocMetadata metadata = new DocMetadata();
        DocumentFragment root;

        try
        {
            // The page contents.
            byte[] contentInOctets = content.getContent();
            // Detect the page encodings.
            EncodingDetector detector = new EncodingDetector(this.getConf());
            detector.autoDetectClues(content, true);
            detector.addClue(sniffCharacterEncoding(contentInOctets), "sniffed");
            String encoding = detector.guessEncoding(content, this.defaultCharEncoding);
            metadata.set(AIMEConstants.ORIGINAL_CHAR_ENCODING.getStringConstant(), encoding);
            metadata.set(AIMEConstants.CHAR_ENCODING_FOR_CONVERSION.getStringConstant(), encoding);
            // Convert into an input stream.
            InputSource input = new InputSource(new ByteArrayInputStream(contentInOctets));
            input.setEncoding(encoding);
            // Parse the page and convert to DOM.
            root = this.parse(input);
        }
        catch (IOException | DOMException | SAXException e)
        {
            return new ParseStatus(e).getEmptyParseResult(content.getUrl(), getConf());
        }
        catch (Exception e)
        {
            LOG.warn("Impossible to parse HTML file: " + content.getUrl(), e);

            return new ParseStatus(e).getEmptyParseResult(content.getUrl(), getConf());
        }

        // Get META directives.
        HtmlMetaProcessor.getMetaTags(metaTags, root, base);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Meta Tags for " + base + ": " + metaTags.toString());
        }

        // Check META directives.
        if (!metaTags.getNoIndex())
        { // Is allowed to index.
            text = this.utils.getText(root);
            title = this.utils.getTitle(root);
        }

        // Print the DOM Tree.
        DOMTreeEcho printDOMTree = new DOMTreeEcho();
        printDOMTree.computeTree(root);
        printDOMTree.printTree();

        // Detect blocks.
        WebContentBlockDetection wcbd = new WebContentBlockDetection();
        List<Block> contentBlocksTmp = wcbd.detectBlocks(title, root, this.getConf());
        contentBlocks = contentBlocksTmp.toArray(new Block[contentBlocksTmp.size()]);

        // Check to see if it's allowed to follow links.
        if (!metaTags.getNoFollow())
        {
            ArrayList<Outlink> l = new ArrayList<>();
            URL baseTag = this.utils.getBase(root);

            this.utils.getOutlinks(baseTag != null ? baseTag : base, l, root);
            outlinks = l.toArray(new Outlink[l.size()]);
        }

        ParseStatus status = new ParseStatus(ParseStatus.SUCCESS);

        if (metaTags.getRefresh())
        {
            status.setMinorCode(ParseStatus.SUCCESS_REDIRECT);
            status.setArgs(new String[]
            {
                metaTags.getRefreshHref().toString(), Integer.toString(metaTags.getRefreshTime())
            });
        }

        ParseData parseData = new ParseData(status, title, outlinks, content.getMetadata(), metadata, contentBlocks);
        ParseResult parseResult = ParseResult.createParseResult(content.getUrl(), new ParseImplementation(text, parseData));

        // Run filters.
        ParseResult filteredParse = this.htmlParseFilters.filter(content, parseResult, metaTags, root);

        if (metaTags.getNoCache())
        { // Don't cache.
            for (Map.Entry<org.apache.hadoop.io.Text, Parse> entry : filteredParse)
            {
                entry.getValue().getData().getParseMeta().set(AIMEConstants.CACHING_FORBIDDEN_KEY.getStringConstant(), cachingPolicy);
            }
        }

        if (LOG.isInfoEnabled())
        {
            LOG.info("Parsing Web Page: " + GeneralUtilities.trimURL(content.getUrl(), 120));
        }

        return filteredParse;
    }

    @Override
    public DocumentFragment getPageStructure(Content document)
    {
        DocMetadata metadata = new DocMetadata();
        DocumentFragment root = null;

        try
        {
            // The page contents.
            byte[] contentInOctets = document.getContent();
            // Detect the page encodings.
            EncodingDetector detector = new EncodingDetector(this.getConf());
            detector.autoDetectClues(document, true);
            detector.addClue(sniffCharacterEncoding(contentInOctets), "sniffed");
            String encoding = detector.guessEncoding(document, this.defaultCharEncoding);
            metadata.set(AIMEConstants.ORIGINAL_CHAR_ENCODING.getStringConstant(), encoding);
            metadata.set(AIMEConstants.CHAR_ENCODING_FOR_CONVERSION.getStringConstant(), encoding);
            // Convert into an input stream.
            InputSource input = new InputSource(new ByteArrayInputStream(contentInOctets));
            input.setEncoding(encoding);
            // Parse the page and convert to DOM.
            root = this.parse(input);
        }
        catch (Exception e)
        {
            LOG.fatal("Impossible to parse HTML file: " + document.getUrl(), e);
        }

        return root;
    }

    /**
     * Parse the HTML document.
     *
     * @param input The document's content.
     *
     * @return The parsed document.
     *
     * @throws Exception A generic exception.
     */
    DocumentFragment parse(InputSource input) throws Exception
    {
        return this.parseTagSoup(input);
    }

    /**
     * Parse the HTML document using the TagSoup parser.
     *
     * @param input The document's content.
     *
     * @return The parsed document.
     *
     * @throws Exception A generic exception.
     */
    private DocumentFragment parseTagSoup(InputSource input) throws Exception
    {
        HTMLDocumentImpl doc = new HTMLDocumentImpl();
        DocumentFragment frag = doc.createDocumentFragment();
        DOMBuilder builder = new DOMBuilder(doc, frag);

        org.ccil.cowan.tagsoup.Parser reader = new org.ccil.cowan.tagsoup.Parser();
        reader.setContentHandler(builder);
        reader.setFeature(org.ccil.cowan.tagsoup.Parser.ignoreBogonsFeature, true);
        reader.setFeature(org.ccil.cowan.tagsoup.Parser.bogonsEmptyFeature, false);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", builder);
        reader.parse(input);

        return frag;
    }
}
