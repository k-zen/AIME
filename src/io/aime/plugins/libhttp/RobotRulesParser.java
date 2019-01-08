package io.aime.plugins.libhttp;

// AIME
import io.aime.crawl.CrawlDatum;
import io.aime.net.protocols.Response;
import io.aime.protocol.ProtocolException;
import io.aime.protocol.RobotRules;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.io.Text;

// IO
import java.io.IOException;

// Log4j
import org.apache.log4j.Logger;

// Net
import java.net.URL;
import java.net.URLDecoder;

// Util
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This class handles the parsing of
 * <code>robots.txt</code> files.
 *
 * <p>It emits RobotRules objects, which describe the download permissions as
 * described in RobotRulesParser.</p>
 *
 * @author Tom Pierce
 * @author Mike Cafarella
 * @author Doug Cutting
 * @author K-Zen
 */
public final class RobotRulesParser implements Configurable {

    private static final String KEY = RobotRulesParser.class.getName();
    private static final Logger LOG = Logger.getLogger(KEY);
    private static final HashMap<String, RobotRuleSet> CACHE = new HashMap<String, RobotRuleSet>();
    private static final String CHARACTER_ENCODING = "UTF-8";
    private static final int NO_PRECEDENCE = Integer.MAX_VALUE;
    private static final RobotRuleSet EMPTY_RULES = new RobotRuleSet();
    private static RobotRuleSet FORBID_ALL_RULES = getForbidAllRules();
    private boolean allowForbidden = false;
    private Configuration conf;
    private Map<String, Integer> robotNames;

    public RobotRulesParser() {
    }

    public RobotRulesParser(Configuration conf) {
        this.setConf(conf);
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
        this.allowForbidden = conf.getBoolean("http.robots.403.allow", false);

        StringTokenizer tok = new StringTokenizer(conf.get("http.robots.agents"), ",");
        List<String> agents = new ArrayList<String>();

        while (tok.hasMoreTokens()) {
            agents.add(tok.nextToken().trim());
        }

        this.setRobotNames(agents.toArray(new String[agents.size()]));
    }

    @Override
    public Configuration getConf() {
        return this.conf;
    }

    /**
     * This class holds the rules which were parsed from a robots.txt file, and
     * can test paths against those rules.
     */
    public static class RobotRuleSet implements RobotRules {

        List<RobotsEntry> tmpEntries = new ArrayList<RobotsEntry>();
        RobotsEntry[] entries = null;
        long expireTime;
        long crawlDelay = -1;

        private class RobotsEntry {

            String prefix;
            boolean allowed;

            RobotsEntry(String prefix, boolean allowed) {
                this.prefix = prefix;
                this.allowed = allowed;
            }
        }

        private void addPrefix(String prefix, boolean allow) {
            if (this.tmpEntries == null) {
                this.tmpEntries = new ArrayList<RobotsEntry>();

                if (this.entries != null) {
                    this.tmpEntries.addAll(Arrays.asList(this.entries));
                }

                this.entries = null;
            }

            this.tmpEntries.add(new RobotsEntry(prefix, allow));
        }

        private void clearPrefixes() {
            if (this.tmpEntries == null) {
                this.tmpEntries = new ArrayList<RobotsEntry>();
                this.entries = null;
            }
            else {
                this.tmpEntries.clear();
            }
        }

        /**
         * Change when the ruleset goes stale.
         *
         * @param expireTime
         */
        public void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }

        /**
         * Get expire time.
         *
         * @return
         */
        @Override
        public long getExpireTime() {
            return this.expireTime;
        }

        @Override
        public long getCrawlDelay() {
            return this.crawlDelay;
        }

        public void setCrawlDelay(long crawlDelay) {
            this.crawlDelay = crawlDelay;
        }

        @Override
        public boolean isAllowed(URL url) {
            String path = url.getPath(); // check rules

            if ((path == null) || "".equals(path)) {
                path = "/";
            }

            return this.isAllowed(path);
        }

        /**
         * Returns FALSE if the robots.txt file prohibits us from accessing the
         * given path, or TRUE otherwise.
         */
        public boolean isAllowed(String path) {
            try {
                path = URLDecoder.decode(path, CHARACTER_ENCODING);
            }
            catch (Exception e) {
                // just ignore it- we can still try to match 
                // path prefixes
            }

            if (this.entries == null) {
                this.entries = new RobotsEntry[this.tmpEntries.size()];
                this.entries = this.tmpEntries.toArray(this.entries);
                this.tmpEntries = null;
            }

            int pos = 0;
            int end = this.entries.length;
            while (pos < end) {
                if (path.startsWith(this.entries[pos].prefix)) {
                    return this.entries[pos].allowed;
                }

                pos++;
            }

            return true;
        }

        @Override
        public String toString() {
            this.isAllowed("x");  // force String[] representation
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < this.entries.length; i++) {
                if (this.entries[i].allowed) {
                    buf.append("Allow: ").append(this.entries[i].prefix).append(System.getProperty("line.separator"));
                }
                else {
                    buf.append("Disallow: ").append(this.entries[i].prefix).append(System.getProperty("line.separator"));
                }
            }

            return buf.toString();
        }
    }

    private void setRobotNames(String[] robotNames) {
        this.robotNames = new HashMap<String, Integer>();

        for (int i = 0; i < robotNames.length; i++) {
            this.robotNames.put(robotNames[i].toLowerCase(), new Integer(i));
        }

        // always make sure "*" is included
        if (!this.robotNames.containsKey("*")) {
            this.robotNames.put("*", new Integer(robotNames.length));
        }
    }

    /**
     * Creates a new RobotRulesParser which will use the supplied robotNames
     * when choosing which stanza to follow in robots.txt files.
     *
     * <p>Any name in the array may be matched. The order of the
     * <code>robotNames</code> determines the precedence- if many names are
     * matched, only the rules associated with the robot name having the
     * smallest index will be used.</p>
     */
    RobotRulesParser(String[] robotNames) {
        this.setRobotNames(robotNames);
    }

    /**
     * Returns a {@link RobotRuleSet} object which encapsulates the rules parsed
     * from the supplied robotContent.
     */
    RobotRuleSet parseRules(byte[] robotContent) {
        if (robotContent == null) {
            return RobotRulesParser.EMPTY_RULES;
        }

        StringTokenizer lineParser = new StringTokenizer(new String(robotContent), "\n\r");
        RobotRuleSet bestRulesSoFar = null;
        int bestPrecedenceSoFar = RobotRulesParser.NO_PRECEDENCE;
        RobotRuleSet currentRules = new RobotRuleSet();
        int currentPrecedence = RobotRulesParser.NO_PRECEDENCE;
        boolean addRules = false;    // in stanza for our robot
        boolean doneAgents = false;  // detect multiple agent lines

        while (lineParser.hasMoreTokens()) {
            String line = lineParser.nextToken();

            // trim out comments and whitespace
            int hashPos = line.indexOf("#");
            if (hashPos >= 0) {
                line = line.substring(0, hashPos);
            }

            line = line.trim();

            if ((line.length() >= 11) && (line.substring(0, 11).equalsIgnoreCase("User-agent:"))) {
                if (doneAgents) {
                    if (currentPrecedence < bestPrecedenceSoFar) {
                        bestPrecedenceSoFar = currentPrecedence;
                        bestRulesSoFar = currentRules;
                        currentPrecedence = NO_PRECEDENCE;
                        currentRules = new RobotRuleSet();
                    }

                    addRules = false;
                }

                doneAgents = false;
                String agentNames = line.substring(line.indexOf(":") + 1);
                agentNames = agentNames.trim();
                StringTokenizer agentTokenizer = new StringTokenizer(agentNames);

                while (agentTokenizer.hasMoreTokens()) {
                    // for each agent listed, see if it's us:
                    String agentName = agentTokenizer.nextToken().toLowerCase();
                    Integer precedenceInt = this.robotNames.get(agentName);

                    if (precedenceInt != null) {
                        int precedence = precedenceInt.intValue();
                        if ((precedence < currentPrecedence) && (precedence < bestPrecedenceSoFar)) {
                            currentPrecedence = precedence;
                        }
                    }
                }

                if (currentPrecedence < bestPrecedenceSoFar) {
                    addRules = true;
                }
            }
            else if ((line.length() >= 9) && (line.substring(0, 9).equalsIgnoreCase("Disallow:"))) {
                doneAgents = true;
                String path = line.substring(line.indexOf(":") + 1);
                path = path.trim();

                try {
                    path = URLDecoder.decode(path, CHARACTER_ENCODING);
                }
                catch (Exception e) {
                    LOG.warn("Error parsing robots.txt rules. Impossible to decode path: " + path);
                }

                if (path.length() == 0) { // "empty rule"
                    if (addRules) {
                        currentRules.clearPrefixes();
                    }
                }
                else {  // rule with path
                    if (addRules) {
                        currentRules.addPrefix(path, false);
                    }
                }

            }
            else if ((line.length() >= 6) && (line.substring(0, 6).equalsIgnoreCase("Allow:"))) {
                doneAgents = true;
                String path = line.substring(line.indexOf(":") + 1);
                path = path.trim();

                if (path.length() == 0) {
                    // "empty rule"- treat same as empty disallow
                    if (addRules) {
                        currentRules.clearPrefixes();
                    }
                }
                else {  // rule with path
                    if (addRules) {
                        currentRules.addPrefix(path, true);
                    }
                }
            }
            else if ((line.length() >= 12) && (line.substring(0, 12).equalsIgnoreCase("Crawl-Delay:"))) {
                doneAgents = true;

                if (addRules) {
                    long crawlDelay = -1;
                    String delay = line.substring("Crawl-Delay:".length(), line.length()).trim();

                    if (delay.length() > 0) {
                        try {
                            crawlDelay = Long.parseLong(delay) * 1000; // sec to millisec
                        }
                        catch (Exception e) {
                            LOG.warn("Impossible to parse Crawl-Delay. Error: " + e.toString(), e);
                        }

                        currentRules.setCrawlDelay(crawlDelay);
                    }
                }
            }
        }

        if (currentPrecedence < bestPrecedenceSoFar) {
            bestPrecedenceSoFar = currentPrecedence;
            bestRulesSoFar = currentRules;
        }

        if (bestPrecedenceSoFar == NO_PRECEDENCE) {
            return EMPTY_RULES;
        }

        return bestRulesSoFar;
    }

    /**
     * Returns a RobotRuleSet object appropriate for use when the robots.txt
     * file is empty or missing; all requests are allowed.
     */
    static RobotRuleSet getEmptyRules() {
        return RobotRulesParser.EMPTY_RULES;
    }

    /**
     * Returns a RobotRuleSet object appropriate for use when the robots.txt
     * file is not fetched due to a 403/Forbidden response; all requests are
     * disallowed.
     */
    static RobotRuleSet getForbidAllRules() {
        RobotRuleSet rules = new RobotRuleSet();
        rules.addPrefix("", false);

        return rules;
    }

    public RobotRuleSet getRobotRulesSet(HTTPBase http, Text url) {
        URL u = null;

        try {
            u = new URL(url.toString());
        }
        catch (Exception e) {
            return RobotRulesParser.EMPTY_RULES;
        }

        return this.getRobotRulesSet(http, u);
    }

    private RobotRuleSet getRobotRulesSet(HTTPBase http, URL url) {
        String host = url.getHost().toLowerCase(); // normalize to lower case
        RobotRuleSet robotRules = RobotRulesParser.CACHE.get(host);
        boolean cacheRule = true;

        if (robotRules == null) { // cache miss
            URL redir = null;

            if (LOG.isDebugEnabled()) {
                LOG.debug("Skipped cache: " + url);
            }

            try {
                Response response = http.getResponse(new URL(url, "/robots.txt"), new CrawlDatum(), true);

                // try one level of redirection ?
                if (response.getCode() == 301 || response.getCode() == 302) {
                    String redirection = response.getHeader("Location");

                    if (redirection == null) {
                        // some versions of MS IIS are known to mangle this header
                        redirection = response.getHeader("location");
                    }

                    if (redirection != null) {
                        if (!redirection.startsWith("http")) {
                            // RFC says it should be absolute, but apparently it isn't
                            redir = new URL(url, redirection);
                        }
                        else {
                            redir = new URL(redirection);
                        }

                        response = http.getResponse(redir, new CrawlDatum(), true);
                    }
                }

                if (response.getCode() == 200) { // found rules: parse them
                    robotRules = parseRules(response.getContent());
                }
                else if ((response.getCode() == 403) && (!this.allowForbidden)) {
                    robotRules = FORBID_ALL_RULES; // use forbid all
                }
                else if (response.getCode() >= 500) {
                    cacheRule = false;
                    robotRules = EMPTY_RULES;
                }
                else {
                    robotRules = EMPTY_RULES; // use default rules
                }
            }
            catch (Throwable t) {
                LOG.error("Impossible to obtain robots.txt for: " + url + ". Error: " + t.toString(), t);
                cacheRule = false;
                robotRules = RobotRulesParser.EMPTY_RULES;
            }

            if (cacheRule) {
                RobotRulesParser.CACHE.put(host, robotRules); // cache rules for host

                if (redir != null && !redir.getHost().equals(host)) {
                    // cache also for the redirected host
                    RobotRulesParser.CACHE.put(redir.getHost(), robotRules);
                }
            }
        }

        return robotRules;
    }

    public boolean isAllowed(HTTPBase http, URL url) throws ProtocolException, IOException {
        String path = url.getPath(); // check rules
        if ((path == null) || "".equals(path)) {
            path = "/";
        }

        return this.getRobotRulesSet(http, url).isAllowed(path);
    }

    public long getCrawlDelay(HTTPBase http, URL url) throws ProtocolException, IOException {
        return this.getRobotRulesSet(http, url).getCrawlDelay();
    }
}
