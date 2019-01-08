package io.aime.plugins.languageidentifier;

// AIME
import io.aime.plugins.languageidentifier.NGramProfile.NGramEntry;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

// IO
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

// Log4j
import org.apache.log4j.Logger;

// Util
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Map;

/**
 * Identify the language of a content, based on statistical analysis.
 *
 * @see <a href="http://www.w3.org/WAI/ER/IG/ert/iso639.htm">ISO 639 Language
 * Codes</a>
 *
 * @author Sami Siren
 * @author Jerome Charron
 * @author K-Zen
 */
public class LanguageIdentifier {

    private final static int DEFAULT_ANALYSIS_LENGTH = 0; // 0 means full content
    private static final Logger LOG = Logger.getLogger(LanguageIdentifier.class.getName());
    /**
     * The maximum amount of data to analyze
     */
    private int analyzeLength = DEFAULT_ANALYSIS_LENGTH;
    /**
     * A global index of ngrams of all supported languages
     */
    private HashMap<CharSequence, NGramEntry[]> ngramsIdx = new HashMap<CharSequence, NGramEntry[]>();
    /**
     * The NGramProfile used for identification
     */
    private NGramProfile suspect = null;

    public LanguageIdentifier(Configuration conf) {
        // Gets ngram sizes to take into account from the Nutch Config
        int minLength = conf.getInt("lang.ngram.min.length", NGramProfile.DEFAULT_MIN_NGRAM_LENGTH); // Minimum size of NGrams.
        int maxLength = conf.getInt("lang.ngram.max.length", NGramProfile.DEFAULT_MAX_NGRAM_LENGTH); // Maximum size of NGrams.
        // Ensure the min and max values are in an acceptale range
        // (ie min >= DEFAULT_MIN_NGRAM_LENGTH and max <= DEFAULT_MAX_NGRAM_LENGTH)
        maxLength = Math.min(maxLength, NGramProfile.ABSOLUTE_MAX_NGRAM_LENGTH);
        maxLength = Math.max(maxLength, NGramProfile.ABSOLUTE_MIN_NGRAM_LENGTH);
        minLength = Math.max(minLength, NGramProfile.ABSOLUTE_MIN_NGRAM_LENGTH);
        minLength = Math.min(minLength, maxLength);

        // Gets the value of the maximum size of data to analyze
        this.analyzeLength = conf.getInt("lang.analyze.max.length", DEFAULT_ANALYSIS_LENGTH);

        Properties p = new Properties();
        try {
            p.load(this.getClass().getResourceAsStream("/langmappings.properties"));
            Enumeration alllanguages = p.keys();
            HashMap<NGramEntry, List<NGramEntry>> tmpIdx = new HashMap<NGramEntry, List<NGramEntry>>();

            while (alllanguages.hasMoreElements()) {
                String lang = (String) (alllanguages.nextElement());
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("io/aime/plugins/languageidentifier/lang/" + lang + "." + NGramProfile.FILE_EXTENSION);

                if (is != null) {
                    NGramProfile profile = new NGramProfile(lang, minLength, maxLength);

                    try {
                        profile.load(is);
                        List<NGramEntry> ngrams = profile.getSorted();

                        for (int i = 0; i < ngrams.size(); i++) {
                            NGramEntry entry = ngrams.get(i);
                            List<NGramEntry> registered = tmpIdx.get(entry);

                            if (registered == null) {
                                registered = new ArrayList<NGramEntry>();
                                tmpIdx.put(entry, registered);
                            }

                            registered.add(entry);
                            entry.setProfile(profile);
                        }

                        is.close();
                    }
                    catch (IOException e) {
                        LOG.fatal(e.getMessage());
                    }
                }
            }

            // transform all ngrams lists to arrays for performances
            Iterator<NGramEntry> keys = tmpIdx.keySet().iterator();
            while (keys.hasNext()) {
                NGramEntry entry = keys.next();
                List<NGramEntry> l = tmpIdx.get(entry);

                if (l != null) {
                    NGramEntry[] array = l.toArray(new NGramEntry[l.size()]);
                    this.ngramsIdx.put(entry.getSeq(), array);
                }
            }

            // Create the suspect profile
            this.suspect = new NGramProfile("suspect", minLength, maxLength);
        }
        catch (Exception e) {
            LOG.fatal("Error accessing language plugin files. Error: " + e.toString(), e);
        }
    }

    /**
     * Identify language of a content.
     *
     * @param content is the content to analyze.
     *
     * @return The 2 letter
     * <a href="http://www.w3.org/WAI/ER/IG/ert/iso639.htm">ISO 639 language
     * code</a> (en, fi, sv, ...) of the language that best matches the
     * specified content.
     */
    public String identify(String content) {
        return this.identify(new StringBuilder(content));
    }

    /**
     * Identify language of a content.
     *
     * @param content is the content to analyze.
     *
     * @return The 2 letter
     * <a href="http://www.w3.org/WAI/ER/IG/ert/iso639.htm">ISO 639 language
     * code</a> (en, fi, sv, ...) of the language that best matches the
     * specified content.
     */
    public String identify(StringBuilder content) {
        StringBuilder text = content;
        if ((analyzeLength > 0) && (content.length() > analyzeLength)) {
            text = new StringBuilder().append(content);
            text.setLength(analyzeLength);
        }

        this.suspect.analyze(text);
        Iterator<NGramEntry> iter = this.suspect.getSorted().iterator();
        float topscore = Float.MIN_VALUE;
        String lang = "";
        HashMap<NGramProfile, Float> scores = new HashMap<NGramProfile, Float>();
        NGramEntry searched = null;

        while (iter.hasNext()) {
            searched = iter.next();
            NGramEntry[] ngrams = ngramsIdx.get(searched.getSeq());

            if (ngrams != null) {
                for (int j = 0; j < ngrams.length; j++) {
                    NGramProfile profile = ngrams[j].getProfile();
                    Float pScore = scores.get(profile);
                    if (pScore == null) {
                        pScore = new Float(0);
                    }
                    float plScore = pScore.floatValue();
                    plScore += ngrams[j].getFrequency() + searched.getFrequency();
                    scores.put(profile, new Float(plScore));
                    if (plScore > topscore) {
                        topscore = plScore;
                        lang = profile.getName();
                    }
                }
            }
        }

        return lang;
    }

    /**
     * Identify language from input stream. This method uses the platform
     * default encoding to read the input stream. For using a specific encoding,
     * use the {@link #identify(InputStream, String)} method.
     *
     * @param is is the input stream to analyze.
     *
     * @return The 2 letter
     * <a href="http://www.w3.org/WAI/ER/IG/ert/iso639.htm">ISO 639 language
     * code</a> (en, fi, sv, ...) of the language that best matches the content
     * of the specified input stream.
     *
     * @throws IOException if something wrong occurs on the input stream.
     */
    public String identify(InputStream is) throws IOException {
        return this.identify(is, null);
    }

    /**
     * Identify language from input stream.
     *
     * @param is      is the input stream to analyze.
     * @param charset is the charset to use to read the input stream.
     *
     * @return The 2 letter
     * <a href="http://www.w3.org/WAI/ER/IG/ert/iso639.htm">ISO 639 language
     * code</a> (en, fi, sv, ...) of the language that best matches the content
     * of the specified input stream.
     *
     * @throws IOException if something wrong occurs on the input stream.
     */
    public String identify(InputStream is, String charset) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int len = 0;

        while (((len = is.read(buffer)) != -1) && ((analyzeLength == 0) || (out.size() < analyzeLength))) {
            if (analyzeLength != 0) {
                len = Math.min(len, analyzeLength - out.size());
            }

            out.write(buffer, 0, len);
        }

        return this.identify((charset == null) ? out.toString() : out.toString(charset));
    }

    /**
     * This method receives a language code abreviation and returns the full
     * language name.
     *
     * @param abbreviation The 2 letter code abreviation. i.e. en = English
     *
     * @return The full language name.
     */
    public static String getFullLanguage(String abbreviation) {
        Properties p = new Properties();

        try {
            p.load(LanguageIdentifier.class.getClass().getResourceAsStream("/langmappings.properties"));

            return (p.get(abbreviation) != null) ? (String) p.get(abbreviation) : "N/A";
        }
        catch (Exception e) {
            LOG.fatal("Error computing full language name. Error: " + e.toString(), e);
        }

        return "N/A";
    }

    /**
     * This method receives a full language name and returns the code
     * abbreviation.
     *
     * @param fullLangName The full language name.
     *
     * @return The 2 letter code abreviation. i.e. en = English
     */
    public static String getAbbreviation(String fullLangName) {
        Properties p = new Properties();
        Map<String, String> revProp = new HashMap<String, String>();

        try {
            p.load(LanguageIdentifier.class.getClass().getResourceAsStream("/langmappings.properties"));

            for (Map.Entry<Object, Object> entry : p.entrySet()) {
                revProp.put((String) entry.getValue(), (String) entry.getKey());
            }

            return (revProp.get(fullLangName) != null) ? (String) revProp.get(fullLangName) : "N/A";
        }
        catch (Exception e) {
            LOG.fatal("Error computing full language name. Error: " + e.toString(), e);
        }

        return "N/A";
    }
}
