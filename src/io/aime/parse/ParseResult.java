package io.aime.parse;

// Apache Hadoop
import org.apache.hadoop.io.Text;

// Log4j
import org.apache.log4j.Logger;

// Util
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A utility class that stores result of a parse. Internally a ParseResult
 * stores
 * &lt;{@link io.aime.parse.ParseText}, {@link io.aime.parse.ParseData}&gt;
 * pairs.
 *
 * <p>Parsers may return multiple results, which correspond to parts or other
 * associated documents related to the original URL.</p>
 *
 * <p>There will be usually one parse result that corresponds directly to the
 * original URL, and possibly many (or none) results that correspond to derived
 * URLs (or sub-URLs).</p>
 *
 * @author Nutch.org
 * @author K-Zen
 */
public class ParseResult implements Iterable<Map.Entry<Text, Parse>> {

    private Map<Text, Parse> parseMap;
    private String originalURL;
    private static final String KEY = ParseResult.class.getName();
    private static final Logger LOG = Logger.getLogger(KEY);

    public ParseResult(String originalUrl) {
        this.parseMap = new HashMap<Text, Parse>();
        this.originalURL = originalUrl;
    }

    public static ParseResult createParseResult(String url, Parse parse) {
        ParseResult parseResult = new ParseResult(url);
        parseResult.put(new Text(url), new ParseText(parse.getText()), parse.getData());

        return parseResult;
    }

    public boolean isEmpty() {
        return this.parseMap.isEmpty();
    }

    public int size() {
        return this.parseMap.size();
    }

    public Parse get(String key) {
        return this.get(new Text(key));
    }

    public Parse get(Text key) {
        return this.parseMap.get(key);
    }

    public void put(Text key, ParseText text, ParseData data) {
        this.put(key.toString(), text, data);
    }

    public void put(String key, ParseText text, ParseData data) {
        this.parseMap.put(new Text(key), new ParseImplementation(text, data, key.equals(originalURL)));
    }

    @Override
    public Iterator<Entry<Text, Parse>> iterator() {
        return this.parseMap.entrySet().iterator();
    }

    public void filter() {
        for (Iterator<Entry<Text, Parse>> i = this.iterator(); i.hasNext();) {
            Entry<Text, Parse> entry = i.next();

            if (!entry.getValue().getData().getStatus().isSuccess()) {
                LOG.warn(entry.getKey() + " not correctly parsed, filtering.");
                i.remove();
            }
        }

    }

    public boolean isSuccess() {
        for (Iterator<Entry<Text, Parse>> i = this.iterator(); i.hasNext();) {
            Entry<Text, Parse> entry = i.next();

            if (!entry.getValue().getData().getStatus().isSuccess()) {
                return false;
            }
        }

        return true;
    }
}
