package io.aime.parse;

// Net
import java.net.URL;

// Util
import java.util.Iterator;
import java.util.Properties;

/**
 * This class holds the information about HTML "meta" tags extracted from a
 * page. Some special tags have convenience methods for easy checking.
 */
public class HtmlMetaTags {

    private boolean noIndex = false;
    private boolean noFollow = false;
    private boolean noCache = false;
    private URL baseHref = null;
    private boolean refresh = false;
    private int refreshTime = 0;
    private URL refreshHref = null;
    private Properties generalTags = new Properties();
    private Properties httpEquivTags = new Properties();

    /**
     * Sets all boolean values to
     * <code>false</code>. Clears all other tags.
     */
    public void reset() {
        noIndex = false;
        noFollow = false;
        noCache = false;
        refresh = false;
        refreshTime = 0;
        baseHref = null;
        refreshHref = null;
        generalTags.clear();
        httpEquivTags.clear();
    }

    /**
     * Sets
     * <code>noFollow</code> to
     * <code>true</code>.
     */
    public void setNoFollow() {
        noFollow = true;
    }

    /**
     * Sets
     * <code>noIndex</code> to
     * <code>true</code>.
     */
    public void setNoIndex() {
        noIndex = true;
    }

    /**
     * Sets
     * <code>noCache</code> to
     * <code>true</code>.
     */
    public void setNoCache() {
        noCache = true;
    }

    /**
     * Sets
     * <code>refresh</code> to the supplied value.
     * @param refresh
     */
    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    /**
     * Sets the
     * <code>baseHref</code>.
     * @param baseHref
     */
    public void setBaseHref(URL baseHref) {
        this.baseHref = baseHref;
    }

    /**
     * Sets the
     * <code>refreshHref</code>.
     * @param refreshHref
     */
    public void setRefreshHref(URL refreshHref) {
        this.refreshHref = refreshHref;
    }

    /**
     * Sets the
     * <code>refreshTime</code>.
     * @param refreshTime
     */
    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }

    /**
     * A convenience method. Returns the current value of
     * <code>noIndex</code>.
     * @return
     */
    public boolean getNoIndex() {
        return noIndex;
    }

    /**
     * A convenience method. Returns the current value of
     * <code>noFollow</code>.
     * @return
     */
    public boolean getNoFollow() {
        return noFollow;
    }

    /**
     * A convenience method. Returns the current value of
     * <code>noCache</code>.
     * @return
     */
    public boolean getNoCache() {
        return noCache;
    }

    /**
     * A convenience method. Returns the current value of
     * <code>refresh</code>.
     * @return
     */
    public boolean getRefresh() {
        return refresh;
    }

    /**
     * A convenience method. Returns the
     * <code>baseHref</code>, if set, or
     * <code>null</code> otherwise.
     * @return
     */
    public URL getBaseHref() {
        return baseHref;
    }

    /**
     * A convenience method. Returns the
     * <code>refreshHref</code>, if set, or
     * <code>null</code> otherwise. The value may be invalid if
     * {@link #getRefresh()}returns
     * <code>false</code>.
     * @return
     */
    public URL getRefreshHref() {
        return refreshHref;
    }

    /**
     * A convenience method. Returns the current value of
     * <code>refreshTime</code>. The value may be invalid if
     * {@link #getRefresh()}returns
     * <code>false</code>.
     * @return
     */
    public int getRefreshTime() {
        return refreshTime;
    }

    /**
     * Returns all collected values of the general meta tags. Property names are
     * tag names, property values are "content" values.
     * @return
     */
    public Properties getGeneralTags() {
        return generalTags;
    }

    /**
     * Returns all collected values of the "http-equiv" meta tags. Property
     * names are tag names, property values are "content" values.
     * @return
     */
    public Properties getHttpEquivTags() {
        return httpEquivTags;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("base=" + baseHref + ", noCache=" + noCache + ", noFollow=" + noFollow + ", noIndex=" + noIndex + ", refresh=" + refresh + ", refreshHref=" + refreshHref + "\n");
        sb.append(" * general tags:\n");
        Iterator it = generalTags.keySet().iterator();

        while (it.hasNext()) {
            String key = (String) it.next();
            sb.append("   - " + key + "\t=\t" + generalTags.get(key) + "\n");
        }

        sb.append(" * http-equiv tags:\n");
        it = httpEquivTags.keySet().iterator();

        while (it.hasNext()) {
            String key = (String) it.next();
            sb.append("   - " + key + "\t=\t" + httpEquivTags.get(key) + "\n");
        }

        return sb.toString();
    }
}
