package io.aime.plugins.urlnormalizerbasic;

// AIME
import io.aime.net.URLNormalizer;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

// Jakarta ORO
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Pattern;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;

// Log4j
import org.apache.log4j.Logger;

// Net
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Converts URLs to a normal form.
 */
public class BasicURLNormalizer implements URLNormalizer {

    private static final String KEY = BasicURLNormalizer.class.getName();
    private static final Logger LOG = Logger.getLogger(KEY);
    private Perl5Compiler compiler = new Perl5Compiler();
    private Rule relativePathRule = null;
    private Rule leadingRelativePathRule = null;
    private Rule adjacentSlashRule = null;
    private Configuration conf;
    private ThreadLocal matchers = new ThreadLocal() {
        @Override
        protected synchronized Object initialValue() {
            return new Perl5Matcher();
        }
    };

    public BasicURLNormalizer() {
        try {
            // this pattern tries to find spots like "/xx/../" in the url, which
            // could be replaced by "/" xx consists of chars, different then "/"
            // (slash) and needs to have at least one char different from "."
            relativePathRule = new Rule();
            relativePathRule.pattern = (Perl5Pattern) compiler.compile("(/[^/]*[^/.]{1}[^/]*/\\.\\./)", Perl5Compiler.READ_ONLY_MASK);
            relativePathRule.substitution = new Perl5Substitution("/");
            // this pattern tries to find spots like leading "/../" in the url,
            // which could be replaced by "/"
            leadingRelativePathRule = new Rule();
            leadingRelativePathRule.pattern = (Perl5Pattern) compiler.compile("^(/\\.\\./)+", Perl5Compiler.READ_ONLY_MASK);
            leadingRelativePathRule.substitution = new Perl5Substitution("/");
            // this pattern tries to find spots like "xx//yy" in the url,
            // which could be replaced by a "/"
            adjacentSlashRule = new Rule();
            adjacentSlashRule.pattern = (Perl5Pattern) compiler.compile("/{2,}", Perl5Compiler.READ_ONLY_MASK);
            adjacentSlashRule.substitution = new Perl5Substitution("/");
        }
        catch (MalformedPatternException e) {
            LOG.warn(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String normalize(String urlString, String scope) throws MalformedURLException {
        if ("".equals(urlString)) { // permit empty
            return urlString;
        }

        urlString = urlString.trim(); // remove extra spaces
        URL url = new URL(urlString);
        String protocol = url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();
        String file = url.getFile();
        boolean changed = false;

        if (!urlString.startsWith(protocol)) { // protocol was lowercased
            changed = true;
        }

        if ("http".equals(protocol) || "ftp".equals(protocol)) {
            if (host != null) {
                String newHost = host.toLowerCase(); // lowercase host
                if (!host.equals(newHost)) {
                    host = newHost;
                    changed = true;
                }
            }

            if (port == url.getDefaultPort()) { // uses default port
                port = -1; // so don't specify it
                changed = true;
            }

            if (file == null || "".equals(file)) { // add a slash
                file = "/";
                changed = true;
            }

            if (url.getRef() != null) { // remove the ref
                changed = true;
            }

            // check for unnecessary use of "/../"
            String file2 = substituteUnnecessaryRelativePaths(file);

            if (!file.equals(file2)) {
                changed = true;
                file = file2;
            }

        }

        if (changed) {
            urlString = new URL(protocol, host, port, file).toString();
        }

        return urlString;
    }

    private String substituteUnnecessaryRelativePaths(String file) {
        String fileWorkCopy = file;
        int oldLen = file.length();
        int newLen = oldLen - 1;

        // All substitutions will be done step by step, to ensure that certain
        // constellations will be normalized, too
        //
        // For example: "/aa/bb/../../cc/../foo.html will be normalized in the
        // following manner:
        //   "/aa/bb/../../cc/../foo.html"
        //   "/aa/../cc/../foo.html"
        //   "/cc/../foo.html"
        //   "/foo.html"
        //
        // The normalization also takes care of leading "/../", which will be
        // replaced by "/", because this is a rather a sign of bad webserver
        // configuration than of a wanted link.  For example, urls like
        // "http://www.foo.com/../" should return a http 404 error instead of
        // redirecting to "http://www.foo.com".
        //
        Perl5Matcher matcher = (Perl5Matcher) matchers.get();

        while (oldLen != newLen) {
            // substitue first occurence of "/xx/../" by "/"
            oldLen = fileWorkCopy.length();
            fileWorkCopy = Util.substitute(matcher, relativePathRule.pattern, relativePathRule.substitution, fileWorkCopy, 1);
            // remove leading "/../"
            fileWorkCopy = Util.substitute(matcher, leadingRelativePathRule.pattern, leadingRelativePathRule.substitution, fileWorkCopy, 1);
            // collapse adjacent slashes with "/"
            fileWorkCopy = Util.substitute(matcher, adjacentSlashRule.pattern, adjacentSlashRule.substitution, fileWorkCopy, 1);
            newLen = fileWorkCopy.length();
        }

        return fileWorkCopy;
    }

    /**
     * Class which holds a compiled pattern and its corresponding substition
     * string.
     */
    private static class Rule {

        public Perl5Pattern pattern;
        public Perl5Substitution substitution;
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    @Override
    public Configuration getConf() {
        return this.conf;
    }
}
