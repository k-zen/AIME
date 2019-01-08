package io.aime.util.domain;

/**
 * (From wikipedia) A top-level domain (TLD) is the last part of an Internet
 * domain name; that is, the letters which follow the final dot of any domain
 * name. For example, in the domain name
 * <code>www.website.com</code>, the top-level domain is
 * <code>com</code>.
 *
 * @author Enis Soztutar
 * @see http://www.iana.org/
 * @see http://en.wikipedia.org/wiki/Top-level_domain
 */
public class TopLevelDomain extends DomainSuffix {

    public enum Type {

        INFRASTRUCTURE, GENERIC, COUNTRY
    };
    private Type type;
    private String countryName = null;

    public TopLevelDomain(String domain, Type type, Status status, float boost) {
        super(domain, status, boost);
        this.type = type;
    }

    public TopLevelDomain(String domain, Status status, float boost, String countryName) {
        super(domain, status, boost);
        this.type = Type.COUNTRY;
        this.countryName = countryName;
    }

    public Type getType() {
        return type;
    }

    /**
     * Returns the country name if TLD is Country Code TLD
     *
     * @return country name or null
     */
    public String getCountryName() {
        return countryName;
    }
}
