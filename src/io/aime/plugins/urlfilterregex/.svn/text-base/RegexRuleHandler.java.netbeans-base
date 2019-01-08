package io.aime.plugins.urlfilterregex;

// AIME
import io.aime.util.SeedTools;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

// Util
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

class RegexRuleHandler {

    static final byte DEFAULT_URL_RULE = 0x1;
    static final byte SEED_SITE_RULE = 0x2;

    /**
     * Creates a new {@link RegexRule}.
     *
     * @param sign  Of the regular expression. A TRUE value means that any URL
     *              matching this rule must be included, whereas a FALSE value
     *              means that any URL matching this rule must be excluded.
     * @param regex Is the regular expression associated to this rule.
     *
     * @return The Regex rule
     */
    private RegexRule createRule(boolean sign, String regex) {
        return new RegexRule(sign, regex);
    }

    /**
     * This method creates all the Regex filter rules.
     *
     * @param conf      Configuration's object
     * @param urlRules  URL filter array
     * @param siteRules Site filter array
     *
     * @return An array of rules
     */
    RegexRule[] createDefaultRules(Configuration conf, int ruleType) {
        Map<Byte, String[]> data = new HashMap<>();
        data.put(DEFAULT_URL_RULE, this.processDefaulURLtRule(conf));
        data.put(SEED_SITE_RULE, this.processSeedSiteRule(conf));

        for (Entry<Byte, String[]> e : data.entrySet()) {
            byte identifier = e.getKey();
            String[] d = e.getValue();
            List<RegexRule> rls = new ArrayList<>();
            // If the object is set then overwrite it, else create a new one.
            for (String r : d) {
                if (r.length() == 0) {
                    continue;
                }

                char first = r.charAt(0);
                boolean sign = false;

                switch (first) {
                    case '+':
                        sign = true;
                        break;
                    case '-':
                        sign = false;
                        break;
                    case ' ':
                    case '\n':
                    case '#': // skip blank & comment lines
                        continue;
                    default:
                        RegexURLFilter.LOG.error("Invalid primary character: " + r);
                }

                rls.add(this.createRule(sign, r.substring(1)));
            }

            if (identifier == ruleType) {
                return rls.toArray(new RegexRule[rls.size()]);
            }
        }

        return null;
    }

    /**
     * Process the default rules.
     *
     * @return An array with the default rules.
     */
    private String[] processDefaulURLtRule(Configuration conf) {
        return new String[]{
            "-^(ftp|mailto):", // Skip FTP, File or Mail addresses.
            "-\\.(ico|gif|jpg|jpeg|png|bmp|tiff|eps)$", // Filter images out.
            "-\\.(gz|rar|sit|tar|tgz|zip)$", // Filter compressed files out.
            "-\\.(css|wmf|mpg|rpm|mov|exe|js)$", // Filter out the rest.
            "-[?*!@=]", // Skip characters that are contained into queries.
            // Skip URLs with slash-delimited segment that repeats 2+ times, to break loops.
            // Example:
            // 1. http://www.abc.com.py/blogs/abc-rogue-123/today/today/today/today/week/week/...
            "-/([^/]+)/\\1/"
        };
    }

    /**
     * Process the seeds and returns the site filters.
     * <p>
     * All seeds must be present in these rules in order for the site to be
     * crawled, otherwise it will be denied the right to be crawled.</p>
     *
     * @param sites An array with the site rules.
     */
    private String[] processSeedSiteRule(Configuration conf) {
        List<String> rules = new ArrayList<>();

        // Examples:
        // +^http://www.example.com 
        // +^https://example.com
        // +^file:///Users/akc
        String[] seedsURLs = SeedTools.getURLs();
        for (String url : seedsURLs) {
            String site = url;
            String rule = "+^" + site;
            rules.add(rule);
        }

        return rules.toArray(new String[0]);
    }
}
