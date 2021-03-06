package io.aime.util;

// Apache Commons
import org.apache.commons.lang.StringUtils;

// Util
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is a utility class for handling domains, subdomains, URLs, and
 * others.
 *
 * @author K-Zen
 */
public class Net {

    /**
     * This method extracts the domain name of a given URL.
     *
     * @param url The URL.
     *
     * @return The domain name.
     */
    public static String getDomainFromURL(String url) {
        String domain = "";
        String regex =
               "^(?:http[s]?\\:\\/\\/)?                                             " + // The connection protocol
                "  (?:.*?)                                                           " + // Subdomains
                "  (                                                                 " + // Parenthesis
                "    (?:[-a-z0-9ñ]+)                                                 " + // Domain
                "    (?:\\.)                                                         " + // Divisor
                "    (?:com|edu|gov|int|mil|net|org|biz|info|name|museum|coop|aero)  " + // Extension
                "    (?:\\.py)?                                                      " + // ccTLD (Only Paraguay)
                "  )                                                                 ";  // Close parenthesis
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE | Pattern.DOTALL);
        Matcher m = p.matcher(url.replaceAll("\\t", "").replaceAll("\\n", "").replaceAll("\\r", ""));

        if (m.find()) {
            domain = m.group(1);
            return domain;
        }
        else {
            return domain;
        }
    }

    /**
     * This method removes a given subdomain from a given host name. i.e. -
     * www.clasipar.paraguay.com -> clasipar.paraguay.com
     *
     * @param host              The host name.
     * @param subDomainToRemove The subdomain to remove.
     *
     * @return The clean host name.
     */
    public static String removeSubdomainFromHost(String host, String subDomainToRemove) {
        String hostClean = "";
        String regex = "^" + subDomainToRemove + "(?:\\d{1,2})?.(.*)";
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE | Pattern.DOTALL);
        Matcher m = p.matcher(host.replaceAll("\\t", "").replaceAll("\\n", "").replaceAll("\\r", ""));

        if (m.find()) {
            hostClean = m.group(1);
        }
        else {
            hostClean = host;
        }

        return hostClean;
    }

    /**
     * This method checks to see if a given URL or site is a subdomain or not. A
     * subdomain consists of the following: - clasipar.paraguay.com -
     * www.paraguay.com
     *
     * But if the site is a ccTLD, then 1 level subdomain is allowed, like: -
     * olx.com.py - abc.com.py
     *
     * But not: - www.abc.com.py
     *
     * @param url   The URL or site.
     * @param ccTLD The ccTLD of the URL.
     *
     * @return TRUE if it's subdomain, FALSE otherwise.
     */
    public static boolean isSubdomain(String url, String ccTLD) {
        /*
         * Convert the URL to lowercase before processing.
         */
        String site = url.toLowerCase();

        if (StringUtils.endsWithIgnoreCase(site, ("." + ccTLD.toLowerCase()))) { // Is ccTLD. i.e. olx.com.py
            if (site.split("\\.").length >= 4) { // Is subdomain. i.e. asuncion.olx.com.py
                return true;
            }
            else { // Is a domain. i.e. olx.com.py
                return false;
            }
        }
        else { // Not ccTLD. i.e. paraguay.com
            if (site.split("\\.").length >= 3) { // Is subdomain. i.e. clasipar.paraguay.com
                return true;
            }
            else { // Is a domain. i.e. paraguay.com
                return false;
            }
        }
    }
}
