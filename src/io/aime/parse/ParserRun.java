package io.aime.parse;

// AIME
import io.aime.protocol.Content;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

// DOM
import org.w3c.dom.DocumentFragment;

// Log4j
import org.apache.log4j.Logger;

// Util
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A Utility class containing methods to simply perform parsing utilities such
 * as iterating through a preferred list of {@link Parser}s to obtain
 * {@link io.aime.getParseResult.Parse} objects.
 *
 * @author Nutch.org
 * @author K-Zen
 */
public class ParserRun {

    private static final Logger LOG = Logger.getLogger(ParserRun.class.getName());
    private ParserFactory parserFactory;
    /**
     * Parser timeout set to 30 sec by default. Set -1 to deactivate *
     */
    private int maxParseTime = 30;

    public ParserRun(Configuration conf) {
        this.parserFactory = new ParserFactory(conf);
        this.maxParseTime = conf.getInt("parser.timeout", 30);
    }

    /**
     * Performs a parse by iterating through a List of preferred
     * {@link io.aime.parse.Parser}s until a successful parse is performed and a
     * {@link io.aime.parse.Parse} object is returned.
     *
     * <p>
     * If the parse is unsuccessful, a message is logged to the
     * <code>WARNING</code> level, and an empty ParseResult object is returned.
     * </p>
     *
     * @param content The content to parse.
     *
     * @return A ParseResult object, which contains all parsed data.
     *
     * @throws ParseException If no suitable parser is found to perform the
     *                        parse.
     */
    public ParseResult getParseResult(Content content) throws ParseException {
        Parser[] parsers = null;

        try {
            parsers = this.parserFactory.getParsers(content.getContentType(), content.getUrl() != null ? content.getUrl() : "");
        }
        catch (ParserNotFound e) {
            LOG.warn("Impossible to find suitable parser: " + content.getUrl() + " type [" + content.getContentType() + "].");
            throw new ParseException(e.getMessage());
        }

        ParseResult parseResult = null;
        for (int i = 0; i < parsers.length; i++) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Parsing: " + content.getUrl() + " with parser [" + parsers[i] + "].");
            }

            if (maxParseTime != -1) {
                parseResult = this.runParser(parsers[i], content);
            }
            else {
                parseResult = parsers[i].getParseResult(content);
                if (parseResult != null && !parseResult.isEmpty()) {
                    return parseResult;
                }
            }
        }

        return new ParseStatus(new ParseException("Unable to parse content for URL: " + content.getUrl())).getEmptyParseResult(content.getUrl(), null);
    }

    public DocumentFragment getParseStructure(Content content) throws ParseException {
        Parser[] parsers = null;
        DocumentFragment dom = null;

        try {
            parsers = this.parserFactory.getParsers(content.getContentType(), content.getUrl() != null ? content.getUrl() : "");
        }
        catch (ParserNotFound e) {
            LOG.warn("Impossible to find suitable parser: " + content.getUrl() + " type [" + content.getContentType() + "].");
            throw new ParseException(e.getMessage());
        }

        for (int i = 0; i < parsers.length; i++) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Parsing: " + content.getUrl() + " with parser [" + parsers[i] + "].");
            }

            dom = parsers[i].getPageStructure(content);
        }

        return dom;
    }

    /**
     * This method will launch the parsing process using a new thread.
     *
     * @param p       The parser to be used.
     * @param content The content/document to be parsed.
     *
     * @return A ParseResult object which contains all necessary info.
     */
    private ParseResult runParser(Parser p, Content content) {
        ParseCallable pc = new ParseCallable(p, content);
        FutureTask<ParseResult> task = new FutureTask<ParseResult>(pc);
        ParseResult res = null;
        Thread t = new Thread(task);
        t.start();

        try {
            res = task.get(maxParseTime, TimeUnit.SECONDS);
        }
        catch (TimeoutException e) {
            LOG.warn("Parsing limit reached for: " + content.getUrl() + " with [" + p + "].");
        }
        catch (Exception e) {
            task.cancel(true);
            res = null;
            t.interrupt();
        }
        finally {
            t = null;
            pc = null;
        }

        return res;
    }
}
