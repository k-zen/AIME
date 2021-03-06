package io.aime.net;

import io.aime.plugin.Pluggable;
import io.aime.plugins.urlfilterregex.RegexRule;
import java.io.Externalizable;
import org.apache.hadoop.io.Writable;

/**
 * Interface used to filter URLs throughout AIME.
 *
 * @author K-Zen
 */
public interface URLFilter extends Pluggable, Writable, Externalizable
{

    /**
     * The name of the extension point.
     */
    public static final String X_POINT_ID = URLFilter.class.getName();
    /**
     * The identifier of the RegexURLFilter filter.
     */
    public static final byte REGEX_URL_FILTER = 0x1;

    /**
     * Load the plug-in with default rules, etc.
     *
     * @return The instance
     */
    public URLFilter init();

    /**
     * Filter in/out URLs. This method decides which URLs are suitable for
     * crawling or not.
     *
     * @param url The URL to filter.
     *
     * @return The URL.
     */
    public String filter(String url);

    /**
     * Return the list of default hard-coded URL rules.
     *
     * @return Array of hard-coded URL rules.
     */
    public RegexRule[] getDefaultURLRule();

    /**
     * Return the list of seed based site rules.
     *
     * @return Array of seed based site rules.
     */
    public RegexRule[] getSeedSiteRule();

    /**
     * Return the URL rules of this filter.
     *
     * @return An array of rules for this filter.
     */
    public RegexRule[] getURLRule();

    /**
     * Add a new URL rule.
     *
     * @param sign    The sign of the rule
     * @param pattern The pattern
     *
     * @return TRUE if the rule was added, FALSE otherwise.
     */
    public boolean addURLRule(boolean sign, String pattern);

    /**
     * Removes a URL rule.
     *
     * @param sign    The sign of the rule
     * @param pattern The pattern
     *
     * @return TRUE if the rule was removed, FALSE otherwise.
     */
    public boolean removeURLRule(boolean sign, String pattern);

    /**
     * Return the site rules of this filter.
     *
     * @return An array of rules for this filter.
     */
    public RegexRule[] getSiteRule();

    /**
     * Add a new site rule.
     *
     * @param sign    The sign of the rule
     * @param pattern The pattern
     *
     * @return TRUE if the rule was added, FALSE otherwise.
     */
    public boolean addSiteRule(boolean sign, String pattern);

    /**
     * Removes a site rule.
     *
     * @param sign    The sign of the rule
     * @param pattern The pattern
     *
     * @return TRUE if the rule was removed, FALSE otherwise.
     */
    public boolean removeSiteRule(boolean sign, String pattern);

    /**
     * Return the type of this filter.
     *
     * @return The filter type.
     */
    public byte getType();
}
