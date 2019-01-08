package io.aime.plugins.libhttp;

// AIME
import io.aime.crawl.CrawlDatum;
import io.aime.net.protocols.Response;
import io.aime.protocol.Content;
import io.aime.protocol.Protocol;
import io.aime.protocol.ProtocolException;
import io.aime.protocol.ProtocolOutput;
import io.aime.protocol.ProtocolStatus;
import io.aime.protocol.RobotRules;
import io.aime.util.GZIPUtils;
import io.aime.util.DeflateUtils;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;

// IO
import java.io.IOException;

// Log4j
import org.apache.log4j.Logger;

// Net
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

// Util
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Abstract class that handles the fetching of Web pages through the HTTP
 * protocol.
 *
 * <p>
 * This class is implemented by the class
 * {@link io.aime.plugins.protocolhttpclient.Http}.
 * </p>
 *
 * @author Nutch.org
 * @author K-Zen
 */
public abstract class HTTPBase implements Protocol {

    public static final int BUFFER_SIZE = 8 * 1024;
    private static final byte[] EMPTY_CONTENT = new byte[0];
    private static Logger log = Logger.getLogger(HTTPBase.class.getName());
    /**
     * Maps from host to a Long naming the time it should be unblocked. The Long
     * is zero while the host is in use, then set to now+wait when a request
     * finishes.
     */
    private static Map<String, Long> BLOCKED_ADDR_TO_TIME = new HashMap<String, Long>();
    /**
     * Maps a host to the number of threads accessing that host.
     */
    private static Map<String, Integer> THREADS_PER_HOST_COUNT = new HashMap<String, Integer>();
    /**
     * Queue of blocked hosts. This contains all of the non-zero entries from
     * BLOCKED_ADDR_TO_TIME, ordered by increasing time.
     */
    private static LinkedList<String> BLOCKED_ADDR_QUEUE = new LinkedList<String>();
    private RobotRulesParser robots = null;
    protected String proxyHost = null;
    protected int proxyPort = 8080;
    protected boolean useProxy = false;
    protected int timeout = 10000;
    protected int maxContent = 64 * 1024;
    /**
     * The number of times a thread will delay when trying to fetch a page.
     */
    protected int maxDelays = 3;
    /**
     * The maximum number of threads that should be allowed to access a host at
     * one time.
     */
    protected int maxThreadsPerHost = 1;
    /**
     * The number of seconds the fetcher will delay between successive requests
     * to the same server.
     */
    protected long serverDelay = 1000;
    protected String userAgent = HTTPBase.getAgentString("Aimebot/0.2 (+http://aime.io/robot; aimebot@aime.io)");
    protected String acceptLanguage = "en-us,en-gb,en;q=0.7,*;q=0.3";
    private Configuration conf;
    /**
     * Do we block by IP addresses or by hostnames?
     */
    private boolean byIP = true;
    /**
     * Do we use HTTP/1.1?
     */
    protected boolean useHttp11 = false;
    /**
     * Skip page if Crawl-Delay longer than this value.
     */
    protected long maxCrawlDelay = -1L;
    /**
     * Plugin should handle host blocking internally.
     */
    protected boolean checkBlocking = true;
    /**
     * Plugin should handle robot rules checking internally.
     */
    protected boolean checkRobots = true;

    public HTTPBase() {
        this(null);
    }

    public HTTPBase(Logger logger) {
        if (logger != null) {
            HTTPBase.log = logger;
        }

        robots = new RobotRulesParser();
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
        this.proxyHost = conf.get("http.proxy.host");
        this.proxyPort = conf.getInt("http.proxy.port", 8080);
        this.useProxy = (this.proxyHost != null && this.proxyHost.length() > 0);
        this.timeout = conf.getInt("http.timeout", 10000);
        this.maxContent = conf.getInt("http.content.limit", 64 * 1024);
        this.maxDelays = conf.getInt("http.max.delays", 3);
        this.maxThreadsPerHost = conf.getInt("fetcher.threads.per.host", 1);
        this.userAgent = HTTPBase.getAgentString(conf.get("http.agent.name"));
        this.acceptLanguage = conf.get("http.accept.language", this.acceptLanguage);
        this.serverDelay = (long) (conf.getFloat("fetcher.server.delay", 1.0f) * 1000);
        this.maxCrawlDelay = (long) (conf.getInt("fetcher.max.crawl.delay", -1) * 1000);
        // backward-compatible default setting
        this.byIP = conf.getBoolean("fetcher.threads.per.host.by.ip", true);
        this.useHttp11 = conf.getBoolean("http.useHttp11", false);
        this.robots.setConf(conf);
        this.checkBlocking = conf.getBoolean(Protocol.CHECK_BLOCKING, true);
        this.checkRobots = conf.getBoolean(Protocol.CHECK_ROBOTS, true);
        this.logConf();
    }

    @Override
    public Configuration getConf() {
        return this.conf;
    }

    @Override
    public ProtocolOutput getProtocolOutput(Text url, CrawlDatum datum) {
        String urlString = url.toString();

        try {
            URL u = new URL(urlString);
            long delay = this.serverDelay;

            if (this.checkRobots) {
                try {
                    if (!this.robots.isAllowed(this, u)) {
                        return new ProtocolOutput(null, new ProtocolStatus(ProtocolStatus.ROBOTS_DENIED, url));
                    }
                }
                catch (Throwable e) {
                    log.error("Exception verifying robot rules for: " + url + ". Error: " + e.toString(), e);
                }

                long crawlDelay = this.robots.getCrawlDelay(this, u);
                delay = crawlDelay > 0 ? crawlDelay : this.serverDelay;
            }

            if (this.checkBlocking && this.maxCrawlDelay >= 0 && delay > this.maxCrawlDelay) {
                /*
                 * Skip this page, otherwise the thread would block for too
                 * long.
                 */
                if (log.isInfoEnabled()) {
                    log.info("Skipping: " + u + " exceeds [fetcher.max.crawl.delay], Max.: " + (this.maxCrawlDelay / 1000) + ", Crawl-Delay: " + (delay / 1000));
                }

                return new ProtocolOutput(null, ProtocolStatus.STATUS_WOULDBLOCK);
            }

            String host = null;
            if (this.checkBlocking) {
                try {
                    host = this.blockAddr(u, delay);
                }
                catch (BlockedException be) {
                    return new ProtocolOutput(null, ProtocolStatus.STATUS_BLOCKED);
                }
            }

            Response response;
            try {
                response = this.getResponse(u, datum, false); // make a request
            }
            finally {
                if (this.checkBlocking) {
                    this.unblockAddr(host, delay);
                }
            }

            int code = response.getCode();
            byte[] content = response.getContent();
            Content c = new Content(u.toString(), u.toString(), (content == null ? EMPTY_CONTENT : content), response.getHeader("Content-Type"), response.getHeaders(), this.conf);
            if (code == 200) { // got a good response
                return new ProtocolOutput(c); // return it
            }
            else if (code == 410) { // page is gone
                return new ProtocolOutput(c, new ProtocolStatus(ProtocolStatus.GONE, "Http: " + code + " url=" + url));
            }
            else if (code >= 300 && code < 400) { // handle redirect
                String location = response.getHeader("Location");

                // some broken servers, such as MS IIS, use lowercase header name...
                if (location == null) {
                    location = response.getHeader("location");
                }
                if (location == null) {
                    location = "";
                }

                u = new URL(u, location);
                int protocolStatusCode;

                switch (code) {
                    case 300: // multiple choices, preferred value in Location
                        protocolStatusCode = ProtocolStatus.MOVED;
                        break;
                    case 301: // moved permanently
                    case 305: // use proxy (Location is URL of proxy)
                        protocolStatusCode = ProtocolStatus.MOVED;
                        break;
                    case 302: // found (temporarily moved)
                    case 303: // see other (redirect after POST)
                    case 307: // temporary redirect
                        protocolStatusCode = ProtocolStatus.TEMP_MOVED;
                        break;
                    case 304: // not modified
                        protocolStatusCode = ProtocolStatus.NOTMODIFIED;
                        break;
                    default:
                        protocolStatusCode = ProtocolStatus.MOVED;
                }

                // handle this in the higher layer.
                return new ProtocolOutput(c, new ProtocolStatus(protocolStatusCode, u));
            }
            else if (code == 400) { // bad request, mark as GONE
                if (log.isDebugEnabled()) {
                    log.debug("400 Bad Request: " + u);
                }

                return new ProtocolOutput(c, new ProtocolStatus(ProtocolStatus.GONE, u));
            }
            else if (code == 401) { // requires authorization, but no valid auth provided.
                if (log.isDebugEnabled()) {
                    log.debug("401 Authentication Required: " + u);
                }

                return new ProtocolOutput(c, new ProtocolStatus(ProtocolStatus.ACCESS_DENIED, "Authentication required: " + urlString));
            }
            else if (code == 404) {
                return new ProtocolOutput(c, new ProtocolStatus(ProtocolStatus.NOTFOUND, u));
            }
            else if (code == 410) { // permanently GONE
                return new ProtocolOutput(c, new ProtocolStatus(ProtocolStatus.GONE, u));
            }
            else {
                return new ProtocolOutput(c, new ProtocolStatus(ProtocolStatus.EXCEPTION, "Http code=" + code + ", url=" + u));
            }
        }
        catch (Throwable e) {
            log.error("Impossible to fetch the page: " + urlString + ". Error: " + e.toString(), e);

            return new ProtocolOutput(null, new ProtocolStatus(e));
        }
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public boolean useProxy() {
        return useProxy;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getMaxContent() {
        return maxContent;
    }

    public int getMaxDelays() {
        return maxDelays;
    }

    public int getMaxThreadsPerHost() {
        return maxThreadsPerHost;
    }

    public long getServerDelay() {
        return serverDelay;
    }

    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Value of "Accept-Language" request header sent by AIME.
     *
     * @return The value of the header "Accept-Language" header.
     */
    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    public boolean getUseHttp11() {
        return useHttp11;
    }

    private String blockAddr(URL url, long crawlDelay) throws ProtocolException {
        String host;

        if (byIP) {
            try {
                InetAddress addr = InetAddress.getByName(url.getHost());
                host = addr.getHostAddress();
            }
            catch (UnknownHostException e) {
                // unable to resolve it, so don't fall back to host name
                throw new HTTPException(e);
            }
        }
        else {
            host = url.getHost();
            if (host == null) {
                throw new HTTPException("Unknown host for url: " + url);
            }
            host = host.toLowerCase();
        }

        int delays = 0;
        while (true) {
            HTTPBase.cleanExpiredServerBlocks(); // free held addresses
            Long time;

            synchronized (HTTPBase.BLOCKED_ADDR_TO_TIME) {
                time = HTTPBase.BLOCKED_ADDR_TO_TIME.get(host);
                if (time == null) { // address is free
                    // get # of threads already accessing this addr
                    Integer counter = HTTPBase.THREADS_PER_HOST_COUNT.get(host);
                    int count = (counter == null) ? 0 : counter.intValue();
                    count++; // increment & store
                    HTTPBase.THREADS_PER_HOST_COUNT.put(host, new Integer(count));

                    if (count >= maxThreadsPerHost) {
                        HTTPBase.BLOCKED_ADDR_TO_TIME.put(host, new Long(0)); // block it
                    }

                    return host;
                }
            }

            if (delays == maxDelays) {
                throw new BlockedException("Exceeded [http.max.delays], retry later.");
            }

            long done = time.longValue();
            long now = System.currentTimeMillis();
            long sleep = 0;
            if (done == 0) { // address is still in use
                sleep = crawlDelay; // wait at least delay

            }
            else if (now < done) { // address is on hold
                sleep = done - now; // wait until its free
            }

            try {
                Thread.sleep(sleep);
            }
            catch (InterruptedException e) {
                // Hacer nada.
            }

            delays++;
        }
    }

    private void unblockAddr(String host, long crawlDelay) {
        synchronized (HTTPBase.BLOCKED_ADDR_TO_TIME) {
            int addrCount = (HTTPBase.THREADS_PER_HOST_COUNT.get(host)).intValue();

            if (addrCount == 1) {
                HTTPBase.THREADS_PER_HOST_COUNT.remove(host);
                HTTPBase.BLOCKED_ADDR_QUEUE.addFirst(host);
                HTTPBase.BLOCKED_ADDR_TO_TIME.put(host, new Long(System.currentTimeMillis() + crawlDelay));
            }
            else {
                HTTPBase.THREADS_PER_HOST_COUNT.put(host, new Integer(addrCount - 1));
            }
        }
    }

    private static void cleanExpiredServerBlocks() {
        synchronized (HTTPBase.BLOCKED_ADDR_TO_TIME) {
            for (int i = HTTPBase.BLOCKED_ADDR_QUEUE.size() - 1; i >= 0; i--) {
                String host = HTTPBase.BLOCKED_ADDR_QUEUE.get(i);
                long time = (HTTPBase.BLOCKED_ADDR_TO_TIME.get(host)).longValue();

                if (time <= System.currentTimeMillis()) {
                    HTTPBase.BLOCKED_ADDR_TO_TIME.remove(host);
                    HTTPBase.BLOCKED_ADDR_QUEUE.remove(i);
                }
            }
        }
    }

    private static String getAgentString(String agentName) {
        StringBuilder buf = new StringBuilder();

        // Check if the UserAgent is set.
        if ((agentName == null) || (agentName.trim().length() == 0)) {
            log.error("User-Agent not set in for crawler.");
        }

        buf.append(agentName.trim());

        return buf.toString();
    }

    protected void logConf() {
        if (log.isDebugEnabled()) {
            log.debug("[http.proxy.host]: " + proxyHost);
            log.debug("[http.proxy.port]: " + proxyPort);
            log.debug("[http.timeout]: " + timeout);
            log.debug("[http.content.limit]: " + maxContent);
            log.debug("[http.agent]: " + userAgent);
            log.debug("[http.accept.language]: " + acceptLanguage);
            log.debug("[" + Protocol.CHECK_BLOCKING + "]: " + checkBlocking);
            log.debug("[" + Protocol.CHECK_ROBOTS + "]: " + checkRobots);

            if (checkBlocking) {
                log.debug("[fetcher.server.delay]: " + serverDelay);
                log.debug("[http.max.delays]: " + maxDelays);
            }
        }
    }

    public byte[] processGzipEncoded(byte[] compressed, URL url) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Decompressing ...");
        }

        byte[] content;
        if (this.getMaxContent() >= 0) {
            content = GZIPUtils.unzipBestEffort(compressed, this.getMaxContent());
        }
        else {
            content = GZIPUtils.unzipBestEffort(compressed);
        }

        if (content == null) {
            throw new IOException("Unzip best effort returned null.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Downloading: " + compressed.length + " bytes of compressed content (expanding to [" + content.length + "] bytes) from: " + url);
        }

        return content;
    }

    public byte[] processDeflateEncoded(byte[] compressed, URL url) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Inflating ...");
        }

        byte[] content = DeflateUtils.inflateBestEffort(compressed, this.getMaxContent());

        if (content == null) {
            throw new IOException("Inflate best effort returned null.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Downloading: " + compressed.length + " bytes of compressed content (expanding to [" + content.length + "] bytes) from: " + url);
        }

        return content;
    }

    protected abstract Response getResponse(URL url, CrawlDatum datum, boolean followRedirects) throws ProtocolException, IOException;

    @Override
    public RobotRules getRobotRules(Text url, CrawlDatum datum) {
        return this.robots.getRobotRulesSet(this, url);
    }
}
