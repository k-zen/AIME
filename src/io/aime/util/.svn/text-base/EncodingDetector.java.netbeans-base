package io.aime.util;

// AIME
import io.aime.net.protocols.Response;
import io.aime.protocol.Content;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

// ICU
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

// Log4j
import org.apache.log4j.Logger;

// NIO
import java.nio.charset.Charset;

// Util
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * A simple class for detecting character encodings.
 * <p>
 * Broadly this encompasses two functions, which are distinctly separate:
 * <ol>
 * <li>Auto detecting a set of "clues" from input text.</li>
 * <li>Taking a set of clues and making a "best guess" as to the "real"
 * encoding.</li>
 * </ol>
 * </p>
 *
 * <p>
 * A caller will often have some extra information about what the encoding might
 * be (e.g. from the HTTP header or HTML meta-tags, often wrong but still
 * potentially useful clues). The types of clues may differ from caller to
 * caller. Thus a typical calling sequence is:
 * <ul>
 * <li>Run step (1) to generate a set of auto-detected clues;</li>
 * <li>Combine these clues with the caller-dependent "extra clues"
 * available;</li>
 * <li>Run step (2) to guess what the most probable answer is.</li>
 * </ul>
 * </p>
 *
 * @author Nutch.org
 * @author K-Zen
 */
public class EncodingDetector {

    private class EncodingClue {

        private String value;
        private String source;
        private int confidence;

        public EncodingClue(String value, String source) {
            this(value, source, NO_THRESHOLD);
        }

        public EncodingClue(String value, String source, int confidence) {
            this.value = value.toLowerCase();
            this.source = source;
            this.confidence = confidence;
        }

        public String getSource() {
            return source;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value + " (" + source + ((confidence >= 0) ? ", " + confidence + "% confidence" : "") + ")";
        }

        public boolean isEmpty() {
            return (value == null || "".equals(value));
        }

        public boolean meetsThreshold() {
            return (confidence < 0 || (minConfidence >= 0 && confidence >= minConfidence));
        }
    }
    private static final String KEY = EncodingDetector.class.getName();
    private static final Logger LOG = Logger.getLogger(KEY);
    public static final int NO_THRESHOLD = -1;
    public static final String MIN_CONFIDENCE_KEY = "encodingdetector.charset.min.confidence";
    private static final HashMap<String, String> ALIASES = new HashMap<String, String>();
    private static final HashSet<String> DETECTABLES = new HashSet<String>();
    // CharsetDetector will die without a minimum amount of data.
    private static final int MIN_LENGTH = 4;

    static {
        DETECTABLES.add("text/html");
        DETECTABLES.add("text/plain");
        DETECTABLES.add("text/richtext");
        DETECTABLES.add("text/rtf");
        DETECTABLES.add("text/sgml");
        DETECTABLES.add("text/tab-separated-values");
        DETECTABLES.add("text/xml");
        DETECTABLES.add("application/rss+xml");
        DETECTABLES.add("application/xhtml+xml");
        /*
         * the following map is not an alias mapping table, but maps character
         * encodings which are often used in mislabelled documents to their
         * correct encodings. For instance, there are a lot of documents
         * labelled 'ISO-8859-1' which contain characters not covered by
         * ISO-8859-1 but covered by windows-1252. Because windows-1252 is a
         * superset of ISO-8859-1 (sharing code points for the common part),
         * it's better to treat ISO-8859-1 as synonymous with windows-1252 than
         * to reject, as invalid, documents labelled as ISO-8859-1 that have
         * characters outside ISO-8859-1.
         */
        ALIASES.put("ISO-8859-1", "windows-1252");
        ALIASES.put("EUC-KR", "x-windows-949");
        ALIASES.put("x-EUC-CN", "GB18030");
        ALIASES.put("GBK", "GB18030");
        //ALIASES.put("Big5", "Big5HKSCS");
        //ALIASES.put("TIS620", "Cp874");
        //ALIASES.put("ISO-8859-11", "Cp874");
    }
    private int minConfidence;
    private CharsetDetector detector;
    private List<EncodingClue> clues;

    public EncodingDetector(Configuration conf) {
        minConfidence = conf.getInt(MIN_CONFIDENCE_KEY, -1);
        detector = new CharsetDetector();
        clues = new ArrayList<EncodingClue>();
    }

    public void autoDetectClues(Content content, boolean filter) {
        byte[] data = content.getContent();

        if (minConfidence >= 0 && DETECTABLES.contains(content.getContentType()) && data.length > MIN_LENGTH) {
            CharsetMatch[] matches = null;

            // do all these in a try/catch; setText and detect/detectAll
            // will sometimes throw exceptions
            try {
                detector.enableInputFilter(filter);
                if (data.length > MIN_LENGTH) {
                    detector.setText(data);
                    matches = detector.detectAll();
                }
            }
            catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ICU4j exception, ignoring ...");
                    LOG.debug(e.getMessage(), e);
                }
            }

            if (matches != null) {
                for (CharsetMatch match : matches) {
                    this.addClue(match.getName(), "detect", match.getConfidence());
                }
            }
        }

        // add character encoding coming from HTTP response header
        this.addClue(EncodingDetector.parseCharacterEncoding(content.getMetadata().get(Response.CONTENT_TYPE)), "header");
    }

    public void addClue(String value, String source, int confidence) {
        if (value == null || "".equals(value)) {
            return;
        }
        value = resolveEncodingAlias(value);
        if (value != null) {
            clues.add(new EncodingClue(value, source, confidence));
        }
    }

    public void addClue(String value, String source) {
        this.addClue(value, source, NO_THRESHOLD);
    }

    /**
     * Guess the encoding with the previously specified list of clues.
     *
     * @param content      Content instance
     * @param defaultValue Default encoding to return if no encoding can be
     *                     detected with enough confidence. Note that this will
     * <b>not</b> be normalized with
     * {@link EncodingDetector#resolveEncodingAlias}
     *
     * @return Guessed encoding or defaultValue
     */
    public String guessEncoding(Content content, String defaultValue) {
        /*
         * This algorithm could be replaced by something more sophisticated;
         * ideally we would gather a bunch of data on where various clues
         * (autodetect, HTTP headers, HTML meta tags, etc.) disagree, tag each
         * with the correct answer, and use machine learning/some statistical
         * method to generate a better heuristic.
         */
        String base = content.getBaseUrl();
        this.findDisagreements(base, clues);

        /*
         * Go down the list of encoding "clues". Use a clue if: 1. Has a
         * confidence value which meets our confidence threshold, OR 2. Doesn't
         * meet the threshold, but is the best try, since nothing else is
         * available.
         */
        EncodingClue defaultClue = new EncodingClue(defaultValue, "default");
        EncodingClue bestClue = defaultClue;

        for (EncodingClue clue : clues) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Character set: " + clue);
            }

            String charset = clue.value;
            if (minConfidence >= 0 && clue.confidence >= minConfidence) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Choosing codification: " + charset + " with confidence: " + clue.confidence);
                }

                return EncodingDetector.resolveEncodingAlias(charset).toLowerCase();
            }
            else if (clue.confidence == NO_THRESHOLD && bestClue == defaultClue) {
                bestClue = clue;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Choosing codification: " + bestClue);
        }

        return bestClue.value.toLowerCase();
    }

    /**
     * Clears all clues.
     */
    public void clearClues() {
        clues.clear();
    }

    /**
     * Strictly for analysis, look for "disagreements." The top guess from each
     * source is examined; if these meet the threshold and disagree, then we log
     * the information -- useful for testing or generating training data for a
     * better heuristic.
     */
    private void findDisagreements(String url, List<EncodingClue> newClues) {
        HashSet<String> valsSeen = new HashSet<String>();
        HashSet<String> sourcesSeen = new HashSet<String>();
        boolean disagreement = false;

        for (int i = 0; i < newClues.size(); i++) {
            EncodingClue clue = newClues.get(i);

            if (!clue.isEmpty() && !sourcesSeen.contains(clue.source)) {
                if (valsSeen.size() > 0 && !valsSeen.contains(clue.value) && clue.meetsThreshold()) {
                    disagreement = true;
                }

                if (clue.meetsThreshold()) {
                    valsSeen.add(clue.value);
                }

                sourcesSeen.add(clue.source);
            }
        }

        if (disagreement) {
            // dump all values in case of disagreement
            StringBuffer sb = new StringBuffer();
            sb.append("Disagree: " + url + "; ");

            for (int i = 0; i < newClues.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }

                sb.append(newClues.get(i));
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(sb.toString());
            }
        }
    }

    public static String resolveEncodingAlias(String encoding) {
        if (encoding == null || !Charset.isSupported(encoding)) {
            return null;
        }

        String canonicalName = new String(Charset.forName(encoding).name());

        return ALIASES.containsKey(canonicalName) ? ALIASES.get(canonicalName) : canonicalName;
    }

    /**
     * Parse the character encoding from the specified content type header. If
     * the content type is null, or there is no explicit character encoding,
     * <code>null</code> is returned.
     *
     * <p>
     * This method was copied from org.apache.catalina.util.RequestUtil, which
     * is licensed under the Apache License, Version 2.0 (the "License").
     * </p>
     *
     * @param contentType a content type header
     *
     * @return
     */
    public static String parseCharacterEncoding(String contentType) {
        if (contentType == null) {
            return (null);
        }

        int start = contentType.indexOf("charset=");
        if (start < 0) {
            return (null);
        }

        String encoding = contentType.substring(start + 8);
        int end = encoding.indexOf(';');
        if (end >= 0) {
            encoding = encoding.substring(0, end);
        }

        encoding = encoding.trim();

        if ((encoding.length() > 2) && (encoding.startsWith("\"")) && (encoding.endsWith("\""))) {
            encoding = encoding.substring(1, encoding.length() - 1);
        }

        return (encoding.trim());

    }
}
