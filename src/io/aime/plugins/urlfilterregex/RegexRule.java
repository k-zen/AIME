package io.aime.plugins.urlfilterregex;

// Util
import java.util.regex.Pattern;

/**
 * A regular expression rule.
 *
 * @author K-Zen
 */
public class RegexRule {

    private boolean sign; // TRUE is accept rule, FALSE deny rule.
    private String pattern; // Regex pattern.

    /**
     * Constructs a new regular expression rule.
     *
     * @param sign    Specifies if this rule must filter-in or filter-out URLs.
     *                TRUE means that any URL matching this rule must be
     *                accepted, FALSE otherwise.
     * @param pattern The regex pattern of the rule.
     */
    public RegexRule(Boolean sign, String pattern) {
        this.sign = sign;
        this.pattern = pattern;
    }

    /**
     * Return if this rule is used for filtering-in or out.
     *
     * @return TRUE accept the URL, FALSE deny it.
     */
    boolean accept() {
        return sign;
    }

    /**
     * Checks if a URL matches this rule.
     *
     * @param url The URL to check.
     *
     * @return TRUE, if a match is found, FALSE otherwise.
     */
    boolean match(String url) {
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(url).find();
    }

    public String getPattern() {
        return pattern;
    }

    public Boolean getSign() {
        return sign;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RegexRule)) {
            return false;
        }

        RegexRule other = (RegexRule) o;
        return (pattern.hashCode() == other.pattern.hashCode()) && (sign == other.sign);
    }

    @Override
    public int hashCode() {
        int res = 0;

        res ^= pattern.hashCode();
        res ^= Boolean.valueOf(sign).hashCode();

        return res;
    }
}
