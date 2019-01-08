package io.aime.plugins.protocolhttpclient;

// AIME
import io.aime.crawl.CrawlDatum;
import io.aime.net.protocols.Response;
import io.aime.protocol.ProtocolException;
import io.aime.plugins.libhttp.HTTPBase;

// Apache Commons
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

// DOM
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

// IO
import java.io.InputStream;
import java.io.IOException;

// Log4j
import org.apache.log4j.Logger;

// Net
import java.net.URL;

// SAX
import org.xml.sax.SAXException;

// Util
import java.util.ArrayList;
import java.util.List;

// XML
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * This class is a protocol plugin that configures an HTTP client for Basic,
 * Digest and NTLM authentication schemes for web server as well as proxy
 * server. It takes care of HTTPS protocol as well as cookies in a single fetch
 * session.
 *
 * @author Susam Pal
 * @author K-Zen
 */
public class HTTP extends HTTPBase {

    private static final Logger LOG = Logger.getLogger(HTTP.class.getName());
    private static MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
    private static HttpClient client = new HttpClient(connectionManager);
    private static String defaultUsername;
    private static String defaultPassword;
    private static String defaultRealm;
    private static String defaultScheme;
    private static String authFile;
    private static String agentHost;
    private static boolean authRulesRead = false;
    private static Configuration conf;
    int maxThreadsTotal = 10;
    private String proxyUsername;
    private String proxyPassword;
    private String proxyRealm;

    /**
     * Returns the configured HTTP client.
     *
     * @return HTTP client
     */
    static synchronized HttpClient getClient() {
        return client;
    }

    /**
     * Constructs this plugin.
     */
    public HTTP() {
        super(LOG);
    }

    /**
     * Reads the configuration from the AIME configuration files and sets the
     * configuration.
     *
     * @param conf Configuration
     */
    @Override
    public void setConf(Configuration conf) {
        super.setConf(conf);
        HTTP.conf = conf;
        this.maxThreadsTotal = conf.getInt("fetcher.threads.fetch", 10);
        this.proxyUsername = conf.get("http.proxy.username", "");
        this.proxyPassword = conf.get("http.proxy.password", "");
        this.proxyRealm = conf.get("http.proxy.realm", "");
        HTTP.agentHost = conf.get("http.agent.host", "");
        HTTP.authFile = conf.get("http.auth.file", "");
        this.configureClient();

        try {
            HTTP.setCredentials();
        }
        catch (Exception ex) {
            LOG.fatal("Impossible to read: " + HTTP.authFile + ". Error: " + ex.getMessage(), ex);
        }
    }

    /**
     * Fetches the
     * <code>url</code> with a configured HTTP client and gets the response.
     *
     * @param url      URL to be fetched
     * @param datum    Crawl data
     * @param redirect Follow redirects if and only if true
     *
     * @return HTTP response
     *
     * @throws ProtocolException
     * @throws IOException
     */
    @Override
    protected Response getResponse(URL url, CrawlDatum datum, boolean redirect) throws ProtocolException, IOException {
        this.resolveCredentials(url);

        return new HTTPResponse(this, url, datum, redirect);
    }

    /**
     * Configures the HTTP client
     */
    private void configureClient() {
        // Set up an HTTPS socket factory that accepts self-signed certs.
        Protocol https = new Protocol("https", new DummySSLProtocolSocketFactory(), 443);
        Protocol.registerProtocol("https", https);
        HttpConnectionManagerParams params = HTTP.connectionManager.getParams();
        params.setConnectionTimeout(this.timeout);
        params.setSoTimeout(this.timeout);
        params.setSendBufferSize(HTTP.BUFFER_SIZE);
        params.setReceiveBufferSize(HTTP.BUFFER_SIZE);
        params.setMaxTotalConnections(this.maxThreadsTotal);

        if (this.maxThreadsTotal > this.maxThreadsPerHost) {
            params.setDefaultMaxConnectionsPerHost(this.maxThreadsPerHost);
        }
        else {
            params.setDefaultMaxConnectionsPerHost(this.maxThreadsTotal);
        }

        // executeMethod(HttpMethod) seems to ignore the connection timeout on the connection manager.
        // set it explicitly on the HttpClient.
        HTTP.client.getParams().setConnectionManagerTimeout(this.timeout);

        HostConfiguration hostConf = client.getHostConfiguration();
        List<Header> headers = new ArrayList<Header>();
        headers.add(new Header("User-Agent", this.userAgent));
        headers.add(new Header("Accept-Language", this.acceptLanguage));
        headers.add(new Header("Accept-Charset", "utf-8,ISO-8859-1;q=0.7,*;q=0.7"));
        headers.add(new Header("Accept", "text/html,application/xml;q=0.9,application/xhtml+xml,text/xml;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"));
        headers.add(new Header("Accept-Encoding", "x-gzip, gzip, deflate"));
        hostConf.getParams().setParameter("http.default-headers", headers);

        // HTTP proxy server details
        if (useProxy) {
            hostConf.setProxy(this.proxyHost, this.proxyPort);

            if (this.proxyUsername.length() > 0) {
                AuthScope proxyAuthScope = HTTP.getAuthScope(this.proxyHost, this.proxyPort, this.proxyRealm);
                NTCredentials proxyCredentials = new NTCredentials(this.proxyUsername, this.proxyPassword, HTTP.agentHost, this.proxyRealm);
                HTTP.client.getState().setProxyCredentials(proxyAuthScope, proxyCredentials);
            }
        }
    }

    /**
     * Reads authentication configuration file (defined as 'http.auth.file' in
     * AIME configuration file) and sets the credentials for the configured
     * authentication scopes in the HTTP client object.
     *
     * @throws ParserConfigurationException If a document builder can not be
     *                                      created.
     * @throws SAXException                 If any parsing error occurs.
     * @throws IOException                  If any I/O error occurs.
     */
    private static synchronized void setCredentials() throws ParserConfigurationException, SAXException, IOException {
        if (HTTP.authRulesRead) {
            return;
        }

        HTTP.authRulesRead = true; // Avoid re-attempting to read

        InputStream is = HTTP.conf.getConfResourceAsInputStream(HTTP.authFile);
        if (is != null) {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);

            Element rootElement = doc.getDocumentElement();
            if (!"auth-configuration".equals(rootElement.getTagName())) {
                LOG.warn("File auth-configuration defective: Root element: <" + rootElement.getTagName() + "> found in: " + HTTP.authFile + " - must be <auth-configuration>.");
            }

            // For each set of credentials
            NodeList credList = rootElement.getChildNodes();
            for (int i = 0; i < credList.getLength(); i++) {
                Node credNode = credList.item(i);
                if (!(credNode instanceof Element)) {
                    continue;
                }

                Element credElement = (Element) credNode;
                if (!"credentials".equals(credElement.getTagName())) {
                    LOG.warn("File auth-configuration defective: Root element <" + credElement.getTagName() + "> not recognized in: " + HTTP.authFile + " - waiting <credentials>.");

                    continue;
                }

                String username = credElement.getAttribute("username");
                String password = credElement.getAttribute("password");

                // For each authentication scope
                NodeList scopeList = credElement.getChildNodes();
                for (int j = 0; j < scopeList.getLength(); j++) {
                    Node scopeNode = scopeList.item(j);
                    if (!(scopeNode instanceof Element)) {
                        continue;
                    }

                    Element scopeElement = (Element) scopeNode;

                    if ("default".equals(scopeElement.getTagName())) {
                        // Determine realm and scheme, if any
                        String realm = scopeElement.getAttribute("realm");
                        String scheme = scopeElement.getAttribute("scheme");
                        // Set default credentials
                        HTTP.defaultUsername = username;
                        HTTP.defaultPassword = password;
                        HTTP.defaultRealm = realm;
                        HTTP.defaultScheme = scheme;

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Credentials: Username: " + username + "; added by default for realm: " + realm + "; scheme: " + scheme);
                        }
                    }
                    else if ("authscope".equals(scopeElement.getTagName())) {
                        // Determine authentication scope details
                        String host = scopeElement.getAttribute("host");

                        int port = -1; // For setting port to AuthScope.ANY_PORT
                        try {
                            port = Integer.parseInt(scopeElement.getAttribute("port"));
                        }
                        catch (Exception ex) {
                            // do nothing, port is already set to any port
                        }

                        String realm = scopeElement.getAttribute("realm");
                        String scheme = scopeElement.getAttribute("scheme");

                        // Set credentials for the determined scope
                        AuthScope authScope = HTTP.getAuthScope(host, port, realm, scheme);
                        NTCredentials credentials = new NTCredentials(username, password, HTTP.agentHost, realm);
                        HTTP.client.getState().setCredentials(authScope, credentials);

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Credentials: Username: " + username + "; added to AuthScope; Host: " + host + "; Port: " + port + "; Realm: " + realm + "; Scheme: " + scheme);
                        }
                    }
                    else {
                        LOG.warn("File auth-configuration defective: Element <" + scopeElement.getTagName() + "> not recognized in: " + HTTP.authFile + " - waiting <authscope>.");
                    }
                }

                is.close();
            }
        }
    }

    /**
     * If credentials for the authentication scope determined from the specified
     * <code>url</code> is not already set in the HTTP client, then this method
     * sets the default credentials to fetch the specified
     * <code>url</code>. If credentials are found for the authentication scope,
     * the method returns without altering the client.
     *
     * @param url URL to be fetched
     */
    private void resolveCredentials(URL url) {
        if (HTTP.defaultUsername != null && HTTP.defaultUsername.length() > 0) {
            int port = url.getPort();

            if (port == -1) {
                if ("https".equals(url.getProtocol())) {
                    port = 443;
                }
                else {
                    port = 80;
                }
            }

            AuthScope scope = new AuthScope(url.getHost(), port);

            if (HTTP.client.getState().getCredentials(scope) != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Pre-configured credentials with reach. Host: " + url.getHost() + "; Port: " + port + "; found for URL: " + url);
                }

                // Credentials are already configured, so do nothing and return
                return;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Pre-configured credentials with reach. Host: " + url.getHost() + "; Port: " + port + "; not found for URL: " + url);
            }

            AuthScope serverAuthScope = HTTP.getAuthScope(url.getHost(), port, HTTP.defaultRealm, HTTP.defaultScheme);
            NTCredentials serverCredentials = new NTCredentials(HTTP.defaultUsername, HTTP.defaultPassword, HTTP.agentHost, HTTP.defaultRealm);
            HTTP.client.getState().setCredentials(serverAuthScope, serverCredentials);
        }
    }

    /**
     * Returns an authentication scope for the specified
     * <code>host</code>,
     * <code>port</code>,
     * <code>realm</code> and
     * <code>scheme</code>.
     *
     * @param host   Host name or address.
     * @param port   Port number.
     * @param realm  Authentication realm.
     * @param scheme Authentication scheme.
     */
    private static AuthScope getAuthScope(String host, int port, String realm, String scheme) {
        if (host.length() == 0) {
            host = null;
        }

        if (port < 0) {
            port = -1;
        }

        if (realm.length() == 0) {
            realm = null;
        }

        if (scheme.length() == 0) {
            scheme = null;
        }

        return new AuthScope(host, port, realm, scheme);
    }

    /**
     * Returns an authentication scope for the specified
     * <code>host</code>,
     * <code>port</code> and
     * <code>realm</code>.
     *
     * @param host  Host name or address.
     * @param port  Port number.
     * @param realm Authentication realm.
     */
    private static AuthScope getAuthScope(String host, int port, String realm) {
        return HTTP.getAuthScope(host, port, realm, "");
    }
}
