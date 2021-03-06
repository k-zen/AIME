package io.aime.util.domain;

// Apache Hadoop
import org.apache.hadoop.util.StringUtils;

// IO
import java.io.InputStream;

// Log4j
import org.apache.log4j.Logger;

// Util
import java.util.HashMap;

/**
 * Storage class for
 * <code>DomainSuffix</code> objects Note: this class is singleton
 * @author Enis Soztutar
 */
public class DomainSuffixes {

    // Logs.
    private static final String KEY = DomainSuffixes.class.getName();
    private static final Logger LOG = Logger.getLogger(KEY);
    private HashMap<String, DomainSuffix> domains = new HashMap<String, DomainSuffix>();
    private static DomainSuffixes instance;

    /**
     * private ctor
     */
    private DomainSuffixes() {
        String file = "domain-suffixes.xml";
        InputStream input = this.getClass().getClassLoader().getResourceAsStream(file);

        try {
            new DomainSuffixesReader().read(this, input);
        }
        catch (Exception ex) {
            LOG.warn(StringUtils.stringifyException(ex));
        }
    }

    /**
     * Singleton instance, lazy instantination
     * @return
     */
    public static DomainSuffixes getInstance() {
        if (instance == null) {
            instance = new DomainSuffixes();
        }

        return instance;
    }

    void addDomainSuffix(DomainSuffix tld) {
        domains.put(tld.getDomain(), tld);
    }

    /**
     * return whether the extension is a registered domain entry
     * @param extension
     * @return
     */
    public boolean isDomainSuffix(String extension) {
        return domains.containsKey(extension);
    }

    /**
     * Return the {@link DomainSuffix} object for the extension, if extension is
     * a top level domain returned object will be an instance of
     * {@link TopLevelDomain}
     * @param extension of the domain
     * @return
     */
    public DomainSuffix get(String extension) {
        return domains.get(extension);
    }
}
