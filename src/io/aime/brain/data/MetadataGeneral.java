package io.aime.brain.data;

import io.aime.aimemisc.io.FileStoring;
import io.aime.net.URLFilter;
import io.aime.plugins.urlfilterregex.RegexRule;
import io.aime.plugins.urlfilterregex.RegexURLFilter;
import io.aime.util.AIMEConstants;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;

/**
 * This serializable class will hold all metadata about the app in general.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @see <a href="http://en.wikipedia.org/wiki/Singleton_pattern">Singleton Pattern</a>
 */
public class MetadataGeneral extends Metadata
{

    private static final Logger LOG = Logger.getLogger(MetadataGeneral.class.getName());
    private static volatile MetadataGeneral _INSTANCE = new MetadataGeneral();
    /** Mark if this instance is empty. If TRUE then we must load data from file. */
    private static volatile boolean isEmpty = true;
    /** Empty data object. */
    private Data data = new Data();

    public static MetadataGeneral getInstance()
    {
        return _INSTANCE;
    }

    @Override
    protected void updateInstance(Object newInstance)
    {
        if (newInstance != null) {
            _INSTANCE = (MetadataGeneral) newInstance;
        }
    }

    @Override
    public MetadataGeneral setData(Object data)
    {
        this.data = (Data) data;
        return this;
    }

    @Override
    public MetadataGeneral.Data getData()
    {
        return data;
    }

    @Override
    public Object getEmptyData()
    {
        return new Data();
    }

    @Override
    public void internalWrite(DataOutput out) throws IOException
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Serializing...");
        }

        data.write(out);
    }

    @Override
    public void internalRead(DataInput in) throws IOException
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("De-Serializing...");
        }

        Data d = new Data();
        d.internalRead(in);
        data = d;
    }

    @Override
    public MetadataGeneral read()
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace(getClass().getName() + " : Is reading!");
        }

        if (isEmpty) {
            File f = new File(AIMEConstants.DEFAULT_JOB_DATA_FOLDER.getStringConstant() + "/" + AIMEConstants.METADATA_GENERAL_FILENAME.getStringConstant());
            getInstance().updateInstance((MetadataGeneral) FileStoring.getInstance().readFromFile(
                    f,
                    AIMEConstants.METADATA_ENCRYPT.getBooleanConstant(),
                    AIMEConstants.METADATA_KEY.getStringConstant(),
                    AIMEConstants.METADATA_ENCODING.getStringConstant()));
            isEmpty = false;
        }

        return getInstance();
    }

    @Override
    public void merge(Metadata newData)
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace(getClass().getName() + " : Is merging!");
        }

        FileStoring.getInstance().writeToFile(
                new File(AIMEConstants.DEFAULT_JOB_DATA_FOLDER.getStringConstant() + "/" + AIMEConstants.METADATA_GENERAL_FILENAME.getStringConstant()),
                (MetadataGeneral) newData,
                AIMEConstants.METADATA_ENCRYPT.getBooleanConstant(),
                AIMEConstants.METADATA_KEY.getStringConstant(),
                AIMEConstants.METADATA_ENCODING.getStringConstant());
    }

    public static class Data extends SerializableData
    {

        public static final MetadataMethod AIME_LOCALITY = MetadataMethod.newBuild().setMethodName("AIMELocality");
        public static final MetadataMethod EXECUTION_TYPE = MetadataMethod.newBuild().setMethodName("ExecutionType");
        public static final MetadataMethod HEAP_SIZE = MetadataMethod.newBuild().setMethodName("HeapSize");
        public static final MetadataMethod FILTERS = MetadataMethod.newBuild().setMethodName("Filters");
        public static final MetadataMethod DEFAULT_URL_RULE = MetadataMethod.newBuild().setMethodName("DefaultURLRule");
        public static final MetadataMethod URL_RULE = MetadataMethod.newBuild().setMethodName("URLRule");
        public static final MetadataMethod SEED_SITE_RULE = MetadataMethod.newBuild().setMethodName("SeedSiteRule");
        public static final MetadataMethod SITE_RULE = MetadataMethod.newBuild().setMethodName("SiteRule");
        public static final MetadataMethod SUM_CONN_TIME = MetadataMethod.newBuild().setMethodName("SumConnTime");
        public static final MetadataMethod COU_CONN_TIME = MetadataMethod.newBuild().setMethodName("CouConnTime");
        // ### DATA
        /**
         * States if this is AIME's Central or not. AIME Central is where the
         * Cerebellum is and gets executed in the JobTracker.
         */
        private Boolean aimeLocality = false; // TRUE is AIME Central, FALSE is AIME Node.
        // Initialization variables.
        private Integer executionType = 1; // If the execution is local or distributed.
        private Integer heapSize = 0; // The heap size we are running.
        // Filters & Rules
        private URLFilter[] filters = new URLFilter[0];
        private RegexRule[] defaultURLRule = new RegexRule[0];
        private RegexRule[] urlRule = new RegexRule[0];
        private RegexRule[] seedSiteRule = new RegexRule[0];
        private RegexRule[] siteRule = new RegexRule[0];
        // Cerebellum Stats
        private AtomicLong sumConnTime = new AtomicLong(0); // This is the sum of all connection times.
        private AtomicLong couConnTime = new AtomicLong(0); // This is the counter of all times we connect to cerebellum.
        // ### DATA

        public static Data newBuild()
        {
            return new Data();
        }

        @Override
        public MetadataGeneral.Data getData()
        {
            return this;
        }

        // ### DATA FUNCTIONS
        public SerializableData setAIMELocality(Boolean locality)
        {
            aimeLocality = locality;
            return this;
        }

        public boolean getAIMELocality()
        {
            return aimeLocality;
        }

        public SerializableData setExecutionType(Integer executionType)
        {
            this.executionType = executionType;
            return this;
        }

        public int getExecutionType()
        {
            return executionType;
        }

        public SerializableData setHeapSize(Integer heapSize)
        {
            this.heapSize = heapSize;
            return this;
        }

        public int getHeapSize()
        {
            return heapSize;
        }

        public SerializableData setFilters(URLFilter[] filters)
        {
            this.filters = filters;
            for (URLFilter fil : this.filters) {
                switch (fil.getType()) {
                    case URLFilter.REGEX_URL_FILTER:
                        defaultURLRule = (fil.getDefaultURLRule() != null && fil.getDefaultURLRule().length > 0) ? fil.getDefaultURLRule() : defaultURLRule;
                        urlRule = (fil.getURLRule() != null && fil.getURLRule().length > 0) ? fil.getURLRule() : urlRule;
                        seedSiteRule = (fil.getSeedSiteRule() != null && fil.getSeedSiteRule().length > 0) ? fil.getSeedSiteRule() : seedSiteRule;
                        siteRule = (fil.getSiteRule() != null && fil.getSiteRule().length > 0) ? fil.getSiteRule() : siteRule;
                        break;
                    default:
                        defaultURLRule = (fil.getDefaultURLRule() != null && fil.getDefaultURLRule().length > 0) ? fil.getDefaultURLRule() : defaultURLRule;
                        urlRule = (fil.getURLRule() != null && fil.getURLRule().length > 0) ? fil.getURLRule() : urlRule;
                        seedSiteRule = (fil.getSeedSiteRule() != null && fil.getSeedSiteRule().length > 0) ? fil.getSeedSiteRule() : seedSiteRule;
                        siteRule = (fil.getSiteRule() != null && fil.getSiteRule().length > 0) ? fil.getSiteRule() : siteRule;
                        break;
                }
            }
            return this;
        }

        public URLFilter[] getFilters()
        {
            return filters;
        }

        public SerializableData setDefaultURLRule(RegexRule[] defaultURLRule)
        {
            this.defaultURLRule = defaultURLRule;
            return this;
        }

        public RegexRule[] getDefaultURLRule()
        {
            return defaultURLRule;
        }

        public SerializableData setURLRule(RegexRule[] urlRule)
        {
            this.urlRule = urlRule;
            return this;
        }

        public RegexRule[] getURLRule()
        {
            return urlRule;
        }

        public SerializableData setSeedSiteRule(RegexRule[] seedSiteRule)
        {
            this.seedSiteRule = seedSiteRule;
            return this;
        }

        public RegexRule[] getSeedSiteRule()
        {
            return seedSiteRule;
        }

        public SerializableData setSiteRule(RegexRule[] siteRule)
        {
            this.siteRule = siteRule;
            return this;
        }

        public RegexRule[] getSiteRule()
        {
            return siteRule;
        }

        public SerializableData setSumConnTime(Long data)
        {
            sumConnTime.addAndGet(data);
            return this;
        }

        public long getSumConnTime()
        {
            return sumConnTime.get();
        }

        public SerializableData setCouConnTime(Long data)
        {
            couConnTime.addAndGet(data);
            return this;
        }

        public long getCouConnTime()
        {
            return couConnTime.get();
        }
        // ### DATA FUNCTIONS

        // ### SERIALIZATION FUNCTIONS
        @Override
        protected void internalWrite(DataOutput out) throws IOException
        {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Serializing...");
            }

            out.writeInt(executionType);
            out.writeInt(heapSize);
            out.writeBoolean(aimeLocality);

            out.writeInt(filters.length);
            for (URLFilter filter : filters) {
                out.writeByte(filter.getType());
                filter.write(out);
            }

            out.writeInt(defaultURLRule.length);
            for (RegexRule rule : defaultURLRule) {
                out.writeBoolean(rule.getSign());
                out.writeUTF(rule.getPattern());
            }

            out.writeInt(urlRule.length);
            for (RegexRule rule : urlRule) {
                out.writeBoolean(rule.getSign());
                out.writeUTF(rule.getPattern());
            }

            out.writeInt(seedSiteRule.length);
            for (RegexRule rule : seedSiteRule) {
                out.writeBoolean(rule.getSign());
                out.writeUTF(rule.getPattern());
            }

            out.writeInt(siteRule.length);
            for (RegexRule rule : siteRule) {
                out.writeBoolean(rule.getSign());
                out.writeUTF(rule.getPattern());
            }

            out.writeLong(sumConnTime.get());
            out.writeLong(couConnTime.get());
        }

        @Override
        protected void internalRead(DataInput in) throws IOException
        {
            if (LOG.isTraceEnabled()) {
                LOG.trace("De-Serializing...");
            }

            aimeLocality = in.readBoolean();
            executionType = in.readInt();
            heapSize = in.readInt();

            filters = new URLFilter[in.readInt()];
            for (int k = 0; k < filters.length; k++) {
                byte type = in.readByte();
                switch (type) {
                    case URLFilter.REGEX_URL_FILTER:
                        filters[k] = RegexURLFilter.read(in);
                        break;
                    default:
                        filters[k] = RegexURLFilter.read(in);
                        break;
                }
            }

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

            sumConnTime = new AtomicLong(in.readLong());
            couConnTime = new AtomicLong(in.readLong());
        }
        // ### SERIALIZATION FUNCTIONS
    }
}
