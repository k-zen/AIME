package io.aime.plugins.urlfilterregex;

import io.aime.brain.Brain;
import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.BrainXMLData.Parameter;
import io.aime.brain.data.MetadataGeneral;
import io.aime.brain.xml.Handler;
import io.aime.net.URLFilter;
import io.aime.util.AIMEConfiguration;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * Filters URLs based on a file of regular expressions using the Java Regex
 * implementation.
 */
public class RegexURLFilter implements URLFilter
{

    protected static final Logger LOG = Logger.getLogger(RegexURLFilter.class.getName());
    private RegexRule[] defaultURLRule = new RegexRule[0];
    private RegexRule[] urlRule = new RegexRule[0];
    private RegexRule[] seedSiteRule = new RegexRule[0];
    private RegexRule[] siteRule = new RegexRule[0];

    @Override
    public RegexURLFilter init()
    {
        RegexRuleHandler handler = new RegexRuleHandler();
        defaultURLRule = handler.createDefaultRules(new AIMEConfiguration().create(), RegexRuleHandler.DEFAULT_URL_RULE);
        seedSiteRule = handler.createDefaultRules(new AIMEConfiguration().create(), RegexRuleHandler.SEED_SITE_RULE);

        return this;
    }

    @Override
    public String filter(String url)
    {
        // This filter is AND'ed. That means that only if both filter are allowed
        // this URL can pass.
        boolean acceptURL = true;
        boolean acceptSite = false;

        // The policy here is allow-deny.
        for (RegexRule r : getDefaultURLRule()) {
            if (r.match(url)) {
                acceptURL = r.accept();
            }
        }

        // The policy here is deny-allow.
        for (RegexRule r : getSeedSiteRule()) {
            if (r.match(url)) {
                acceptSite = r.accept();
            }
        }

        // The policy here is allow-deny.
        for (RegexRule r : getURLRule()) {
            if (r.match(url)) {
                acceptURL = r.accept();
            }
        }

        // The policy here is deny-allow.
        for (RegexRule r : getSiteRule()) {
            if (r.match(url)) {
                acceptSite = r.accept();
            }
        }

        return (acceptURL && acceptSite) ? url : null;
    }

    @Override
    public RegexRule[] getURLRule()
    {
        return urlRule;
    }

    @Override
    public RegexRule[] getSiteRule()
    {
        return siteRule;
    }

    @Override
    public byte getType()
    {
        return REGEX_URL_FILTER;
    }

    @Override
    public boolean addURLRule(boolean sign, String pattern)
    {
        Set<RegexRule> ruleList = new LinkedHashSet<>();
        ruleList.addAll(Arrays.asList((RegexRule[]) Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataGeneral.Data.class)
                                .setFunction(MetadataGeneral.Data.URL_RULE.getMethodName()))).get()));

        if (ruleList.add(new RegexRule(sign, pattern))) {
            Brain
                    .getInstance()
                    .execute(Handler
                            .makeXMLRequest(BrainXMLData
                                    .newBuild()
                                    .setJob(BrainXMLData.JOB_MERGE)
                                    .setClazz(MetadataGeneral.Data.class)
                                    .setFunction(MetadataGeneral.Data.URL_RULE.getMethodName())
                                    .setParam(Parameter
                                            .newBuild()
                                            .setType(RegexRule[].class)
                                            .setData(urlRule = ruleList.toArray(new RegexRule[0])))));

            return true;
        }

        return false;
    }

    @Override
    public boolean addSiteRule(boolean sign, String pattern)
    {
        Set<RegexRule> ruleList = new LinkedHashSet<>();
        ruleList.addAll(Arrays.asList((RegexRule[]) Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataGeneral.Data.class)
                                .setFunction(MetadataGeneral.Data.SITE_RULE.getMethodName()))).get()));

        if (ruleList.add(new RegexRule(sign, pattern))) {
            Brain
                    .getInstance()
                    .execute(Handler
                            .makeXMLRequest(BrainXMLData
                                    .newBuild()
                                    .setJob(BrainXMLData.JOB_MERGE)
                                    .setClazz(MetadataGeneral.Data.class)
                                    .setFunction(MetadataGeneral.Data.SITE_RULE.getMethodName())
                                    .setParam(Parameter
                                            .newBuild()
                                            .setType(RegexRule[].class)
                                            .setData(siteRule = ruleList.toArray(new RegexRule[0])))));

            return true;
        }

        return false;
    }

    @Override
    public boolean removeURLRule(boolean sign, String pattern)
    {
        Set<RegexRule> ruleList = new LinkedHashSet<>();
        ruleList.addAll(Arrays.asList((RegexRule[]) Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataGeneral.Data.class)
                                .setFunction(MetadataGeneral.Data.URL_RULE.getMethodName()))).get()));

        if (ruleList.remove(new RegexRule(sign, pattern))) {
            Brain
                    .getInstance()
                    .execute(Handler
                            .makeXMLRequest(BrainXMLData
                                    .newBuild()
                                    .setJob(BrainXMLData.JOB_MERGE)
                                    .setClazz(MetadataGeneral.Data.class)
                                    .setFunction(MetadataGeneral.Data.URL_RULE.getMethodName())
                                    .setParam(Parameter
                                            .newBuild()
                                            .setType(RegexRule[].class)
                                            .setData(urlRule = ruleList.toArray(new RegexRule[0])))));

            return true;
        }

        return false;
    }

    @Override
    public boolean removeSiteRule(boolean sign, String pattern)
    {
        Set<RegexRule> ruleList = new LinkedHashSet<>();
        ruleList.addAll(Arrays.asList((RegexRule[]) Brain
                .getInstance()
                .execute(Handler
                        .makeXMLRequest(BrainXMLData
                                .newBuild()
                                .setJob(BrainXMLData.JOB_REQUEST)
                                .setClazz(MetadataGeneral.Data.class)
                                .setFunction(MetadataGeneral.Data.SITE_RULE.getMethodName()))).get()));

        if (ruleList.remove(new RegexRule(sign, pattern))) {
            Brain
                    .getInstance()
                    .execute(Handler
                            .makeXMLRequest(BrainXMLData
                                    .newBuild()
                                    .setJob(BrainXMLData.JOB_MERGE)
                                    .setClazz(MetadataGeneral.Data.class)
                                    .setFunction(MetadataGeneral.Data.SITE_RULE.getMethodName())
                                    .setParam(Parameter
                                            .newBuild()
                                            .setType(RegexRule[].class)
                                            .setData(siteRule = ruleList.toArray(new RegexRule[0])))));

            return true;
        }

        return false;
    }

    @Override
    public RegexRule[] getDefaultURLRule()
    {
        return defaultURLRule;
    }

    @Override
    public RegexRule[] getSeedSiteRule()
    {
        return seedSiteRule;
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        internalWrite(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        internalRead(in);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        internalWrite(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        internalRead(in);
    }

    private void internalWrite(DataOutput out) throws IOException
    {
        out.writeInt(defaultURLRule.length);
        for (RegexRule r : defaultURLRule) {
            out.writeBoolean(r.getSign());
            out.writeUTF(r.getPattern());
        }

        out.writeInt(urlRule.length);
        for (RegexRule r : urlRule) {
            out.writeBoolean(r.getSign());
            out.writeUTF(r.getPattern());
        }

        out.writeInt(seedSiteRule.length);
        for (RegexRule r : seedSiteRule) {
            out.writeBoolean(r.getSign());
            out.writeUTF(r.getPattern());
        }

        out.writeInt(siteRule.length);
        for (RegexRule r : siteRule) {
            out.writeBoolean(r.getSign());
            out.writeUTF(r.getPattern());
        }
    }

    private void internalRead(DataInput in) throws IOException
    {
        defaultURLRule = new RegexRule[in.readInt()];
        for (int k = 0; k < defaultURLRule.length; k++) {
            defaultURLRule[k] = new RegexRule(in.readBoolean(), in.readUTF());
        }

        urlRule = new RegexRule[in.readInt()];
        for (int k = 0; k < urlRule.length; k++) {
            urlRule[k] = new RegexRule(in.readBoolean(), in.readUTF());
        }

        seedSiteRule = new RegexRule[in.readInt()];
        for (int k = 0; k < seedSiteRule.length; k++) {
            seedSiteRule[k] = new RegexRule(in.readBoolean(), in.readUTF());
        }

        siteRule = new RegexRule[in.readInt()];
        for (int k = 0; k < siteRule.length; k++) {
            siteRule[k] = new RegexRule(in.readBoolean(), in.readUTF());
        }
    }

    public static RegexURLFilter read(DataInput in) throws IOException
    {
        RegexURLFilter filter = new RegexURLFilter();
        filter.readFields(in);

        return filter;
    }
}
