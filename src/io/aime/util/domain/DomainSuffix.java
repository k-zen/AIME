package io.aime.util.domain;

/**
 * This class represents the last part of the host name, which is operated by
 * authoritives, not individuals. This information is needed to find the domain
 * name of a host. The domain name of a host is defined to be the last part
 * before the domain suffix, w/o subdomain names. As an example the domain name
 * of <br><code> http://lucene.apache.org/
 * </code><br> is
 * <code> apache.org</code>
 * <br>
 * This class holds three fields,
 * <strong>domain</strong> field represents the suffix (such as "co.uk")
 * <strong>boost</strong> is a float for boosting score of url's with this
 * suffix
 * <strong>status</strong> field represents domain's status
 * @author Enis Soztutar
 * @see TopLevelDomain
 * @see domain-suffixes.xml
 */
public class DomainSuffix {

    /**
     * Enumeration of the status of the tld. Please see domain-suffixes.xml.
     */
    public enum Status {

        INFRASTRUCTURE, SPONSORED, UNSPONSORED, STARTUP, PROPOSED, DELETED, PSEUDO_DOMAIN, DEPRECATED, IN_USE, NOT_IN_USE, REJECTED
    };
    private String domain;
    private Status status;
    private float boost;
    public static final float DEFAULT_BOOST = 1.0f;
    public static final Status DEFAULT_STATUS = Status.IN_USE;

    public DomainSuffix(String domain, Status status, float boost) {
        this.domain = domain;
        this.status = status;
        this.boost = boost;
    }

    public DomainSuffix(String domain) {
        this(domain, DEFAULT_STATUS, DEFAULT_BOOST);
    }

    public String getDomain() {
        return domain;
    }

    public Status getStatus() {
        return status;
    }

    public float getBoost() {
        return boost;
    }

    @Override
    public String toString() {
        return domain;
    }
}
