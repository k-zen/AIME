package io.aime.crawl;

import io.aime.aimemisc.digest.SignatureComparator;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.VersionMismatchException;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * The crawl state of a document.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class CrawlDatum implements WritableComparable<CrawlDatum>, Cloneable
{

    public static final String GENERATE_DIR_NAME = "crawl_generate";
    public static final String FETCH_DIR_NAME = "crawl_fetch";
    public static final String PARSE_DIR_NAME = "crawl_parse";
    public static final byte STATUS_DB_UNFETCHED = 0x01; // Page was not fetched yet.
    public static final byte STATUS_DB_FETCHED = 0x02; // Page was successfully fetched.
    public static final byte STATUS_DB_GONE = 0x03; // Page no longer exists.
    public static final byte STATUS_DB_REDIR_TEMP = 0x04; // Page temporarily redirects to other page.
    public static final byte STATUS_DB_REDIR_PERM = 0x05; // Page permanently redirects to other page.
    public static final byte STATUS_DB_NOTMODIFIED = 0x06; // Page was successfully fetched and found not modified.
    public static final byte STATUS_DB_MAX = 0x1f; // Maximum value of DB-related status.
    public static final byte STATUS_FETCH_SUCCESS = 0x21; // Fetching was successful.
    public static final byte STATUS_FETCH_RETRY = 0x22; // Fetching unsuccessful, needs to be retried (transient errors).
    public static final byte STATUS_FETCH_REDIR_TEMP = 0x23; // Fetching temporarily redirected to other page.
    public static final byte STATUS_FETCH_REDIR_PERM = 0x24; // Fetching permanently redirected to other page.
    public static final byte STATUS_FETCH_GONE = 0x25; // Fetching unsuccessful - page is gone.
    public static final byte STATUS_FETCH_NOTMODIFIED = 0x26; // Fetching successful - page is not modified.
    public static final byte STATUS_FETCH_MAX = 0x3f; // Maximum value of fetch-related status.
    public static final byte STATUS_SIGNATURE = 0x41; // Page signature.
    public static final byte STATUS_INJECTED = 0x42; // Page was newly injected.
    public static final byte STATUS_LINKED = 0x43; // Page discovered through a link.
    public static final byte STATUS_PARSE_META = 0x44; // Page got metadata from a parser.
    public static final HashMap<Byte, String> statNames = new HashMap<>();
    private static final byte CUR_VERSION = 7;
    private static final int SCORE_OFFSET = 1 + 1 + 8 + 1 + 4;
    private static final int SCORE_GRAVITY_OFFSET = CrawlDatum.SCORE_OFFSET + 4;
    private static final int SIGNATURE_OFFSET = SCORE_GRAVITY_OFFSET + 4 + 8 + 8;
    private static final byte SIGNATURE_LENGTH = 8;
    // Properties of a particular CrawlDatum. All this fields should be serializable.
    private byte status;
    private long fetchTime = System.currentTimeMillis();
    private long discoveryTime;
    private long modifiedTime;
    private byte retries;
    private int fetchInterval;
    private float score = 0.0f;
    private float scoreGravity = 10.0f;
    private long signature;
    private MapWritable metadata;

    static
    {
        CrawlDatum.statNames.put(CrawlDatum.STATUS_DB_UNFETCHED, "db_unfetched");
        CrawlDatum.statNames.put(CrawlDatum.STATUS_DB_FETCHED, "db_fetched");
        CrawlDatum.statNames.put(CrawlDatum.STATUS_DB_GONE, "db_gone");
        CrawlDatum.statNames.put(CrawlDatum.STATUS_DB_REDIR_TEMP, "db_redir_temp");
        CrawlDatum.statNames.put(CrawlDatum.STATUS_DB_REDIR_PERM, "db_redir_perm");
        CrawlDatum.statNames.put(CrawlDatum.STATUS_DB_NOTMODIFIED, "db_notmodified");
        CrawlDatum.statNames.put(CrawlDatum.STATUS_SIGNATURE, "signature");
        CrawlDatum.statNames.put(CrawlDatum.STATUS_INJECTED, "injected");
        CrawlDatum.statNames.put(CrawlDatum.STATUS_LINKED, "linked");
        CrawlDatum.statNames.put(CrawlDatum.STATUS_FETCH_SUCCESS, "fetch_success");
        CrawlDatum.statNames.put(CrawlDatum.STATUS_FETCH_RETRY, "fetch_retry");
        CrawlDatum.statNames.put(CrawlDatum.STATUS_FETCH_REDIR_TEMP, "fetch_redir_temp");
        CrawlDatum.statNames.put(CrawlDatum.STATUS_FETCH_REDIR_PERM, "fetch_redir_perm");
        CrawlDatum.statNames.put(CrawlDatum.STATUS_FETCH_GONE, "fetch_gone");
        CrawlDatum.statNames.put(CrawlDatum.STATUS_FETCH_NOTMODIFIED, "fetch_notmodified");
        CrawlDatum.statNames.put(CrawlDatum.STATUS_PARSE_META, "parse_metadata");
    }

    public CrawlDatum()
    {
    }

    public CrawlDatum(int status, int fetchInterval)
    {
        this();
        this.status = (byte) status;
        this.fetchInterval = fetchInterval;
    }

    public CrawlDatum(int status, int fetchInterval, float score)
    {
        this(status, fetchInterval);
        this.score = score;
    }

    public static boolean hasDBStatus(CrawlDatum datum)
    {
        return datum.status <= STATUS_DB_MAX;
    }

    public static boolean hasFetchStatus(CrawlDatum datum)
    {
        return datum.status > STATUS_DB_MAX && datum.status <= STATUS_FETCH_MAX;
    }

    public byte getStatus()
    {
        return status;
    }

    public static String getStatusName(byte value)
    {
        String res = statNames.get(value);
        if (res == null)
        {
            res = "unknown";
        }

        return res;
    }

    public void setStatus(int status)
    {
        this.status = (byte) status;
    }

    public long getFetchTime()
    {
        return fetchTime;
    }

    public void setFetchTime(long fetchTime)
    {
        this.fetchTime = fetchTime;
    }

    public long getModifiedTime()
    {
        return modifiedTime;
    }

    public void setModifiedTime(long modifiedTime)
    {
        this.modifiedTime = modifiedTime;
    }

    public long getDiscoveryTime()
    {
        return discoveryTime;
    }

    public void setDiscoveryTime(long discoveryTime)
    {
        this.discoveryTime = discoveryTime;
    }

    public byte getRetriesSinceFetch()
    {
        return retries;
    }

    public void setRetriesSinceFetch(int retries)
    {
        this.retries = (byte) retries;
    }

    public int getFetchInterval()
    {
        return fetchInterval;
    }

    public void setFetchInterval(int fetchInterval)
    {
        this.fetchInterval = fetchInterval;
    }

    public void setFetchInterval(float fetchInterval)
    {
        this.fetchInterval = Math.round(fetchInterval);
    }

    public float getScore()
    {
        return score;
    }

    public void setScore(float score)
    {
        this.score = score;
    }

    public float getScoreGravity()
    {
        return scoreGravity;
    }

    public void setScoreGravity(float scoreGravity)
    {
        this.scoreGravity = scoreGravity;
    }

    public long getSignature()
    {
        return signature;
    }

    public void setSignature(long signature)
    {
        this.signature = signature;
    }

    public void setMetadata(MapWritable mapWritable)
    {
        this.metadata = new MapWritable(mapWritable);
    }

    public void putAllMetadata(CrawlDatum other)
    {
        for (Entry<Writable, Writable> e : other.getMetadata().entrySet())
        {
            getMetadata().put(e.getKey(), e.getValue());
        }
    }

    public MapWritable getMetadata()
    {
        if (metadata == null)
        {
            metadata = new org.apache.hadoop.io.MapWritable();
        }

        return metadata;
    }

    public static CrawlDatum read(DataInput in) throws IOException
    {
        CrawlDatum result = new CrawlDatum();
        result.readFields(in);

        return result;
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        byte version = in.readByte(); // read version
        if (version > CUR_VERSION)
        { // check version
            throw new VersionMismatchException(CUR_VERSION, version);
        }

        status = in.readByte();
        fetchTime = in.readLong();
        retries = in.readByte();

        if (version > 5)
        {
            fetchInterval = in.readInt();
        }
        else
        {
            fetchInterval = Math.round(in.readFloat());
        }

        score = in.readFloat();
        scoreGravity = in.readFloat();

        if (version > 2)
        {
            modifiedTime = in.readLong();
            discoveryTime = in.readLong();
            signature = in.readLong();
        }

        if (version > 3)
        {
            boolean hasMetadata = false;

            if (version < 7)
            {
                MapWritable oldMetaData = new MapWritable();

                if (in.readBoolean())
                {
                    hasMetadata = true;
                    metadata = new MapWritable();
                    oldMetaData.readFields(in);
                }

                for (Writable key : oldMetaData.keySet())
                {
                    metadata.put(key, oldMetaData.get(key));
                }
            }
            else
            {
                if (in.readBoolean())
                {
                    hasMetadata = true;
                    metadata = new org.apache.hadoop.io.MapWritable();
                    metadata.readFields(in);
                }
            }

            if (hasMetadata == false)
            {
                metadata = null;
            }
        }
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeByte(CUR_VERSION); // store current version
        out.writeByte(status);
        out.writeLong(fetchTime);
        out.writeByte(retries);
        out.writeInt(fetchInterval);
        out.writeFloat(score);
        out.writeFloat(scoreGravity);
        out.writeLong(modifiedTime);
        out.writeLong(discoveryTime);
        out.writeLong(signature);

        if (metadata != null && metadata.size() > 0)
        {
            out.writeBoolean(true);
            metadata.write(out);
        }
        else
        {
            out.writeBoolean(false);
        }
    }

    /**
     * Copy the content of an old instance to a new one.
     *
     * @param old Old instance.
     */
    public void set(CrawlDatum old)
    {
        status = old.status;
        fetchTime = old.fetchTime;
        retries = old.retries;
        fetchInterval = old.fetchInterval;
        score = old.score;
        scoreGravity = old.scoreGravity;
        modifiedTime = old.modifiedTime;
        discoveryTime = old.discoveryTime;
        signature = old.signature;

        if (old.metadata != null)
        {
            metadata = new org.apache.hadoop.io.MapWritable(old.metadata); // make a deep copy
        }
        else
        {
            metadata = null;
        }
    }

    @Override
    public int compareTo(CrawlDatum that)
    {
        // Sort in descending order. To sort in ascending order
        // reverse the order of the difference.
        // i.e. score - that.score
        if (that.score != score)
        {
            return (that.score - score) > 0 ? 1 : -1;
        }

        if (that.scoreGravity != scoreGravity)
        {
            return (that.scoreGravity - scoreGravity) > 0 ? 1 : -1;
        }

        if (that.status != status)
        {
            return status - that.status;
        }

        if (that.fetchTime != fetchTime)
        {
            return (that.fetchTime - fetchTime) > 0 ? 1 : -1;
        }

        if (that.retries != retries)
        {
            return that.retries - retries;
        }

        if (that.fetchInterval != fetchInterval)
        {
            return (that.fetchInterval - fetchInterval) > 0 ? 1 : -1;
        }

        if (that.modifiedTime != modifiedTime)
        {
            return (that.modifiedTime - modifiedTime) > 0 ? 1 : -1;
        }

        if (that.discoveryTime != discoveryTime)
        {
            return (that.discoveryTime - discoveryTime) > 0 ? 1 : -1;
        }

        return SignatureComparator._compare(this, that);
    }

    public static class Comparator extends WritableComparator
    {

        public Comparator()
        {
            super(CrawlDatum.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2)
        {
            float score1 = Comparator.readFloat(b1, s1 + SCORE_OFFSET);
            float score2 = Comparator.readFloat(b2, s2 + SCORE_OFFSET);
            if (score2 != score1)
            {
                return (score2 - score1) > 0 ? 1 : -1;
            }

            float scoreGravity1 = Comparator.readFloat(b1, s1 + SCORE_GRAVITY_OFFSET);
            float scoreGravity2 = Comparator.readFloat(b2, s2 + SCORE_GRAVITY_OFFSET);
            if (scoreGravity2 != scoreGravity1)
            {
                return (scoreGravity2 - scoreGravity1) > 0 ? 1 : -1;
            }

            int status1 = b1[s1 + 1];
            int status2 = b2[s2 + 1];
            if (status2 != status1)
            {
                return status2 - status1;
            }

            long fetchTime1 = Comparator.readLong(b1, s1 + 1 + 1);
            long fetchTime2 = Comparator.readLong(b2, s2 + 1 + 1);
            if (fetchTime2 != fetchTime1)
            {
                return (fetchTime2 - fetchTime1) > 0 ? 1 : -1;
            }

            int retries1 = b1[s1 + 1 + 1 + 8];
            int retries2 = b2[s2 + 1 + 1 + 8];
            if (retries2 != retries1)
            {
                return retries2 - retries1;
            }

            int fetchInterval1 = Comparator.readInt(b1, s1 + 1 + 1 + 8 + 1);
            int fetchInterval2 = Comparator.readInt(b2, s2 + 1 + 1 + 8 + 1);
            if (fetchInterval2 != fetchInterval1)
            {
                return (fetchInterval2 - fetchInterval1) > 0 ? 1 : -1;
            }

            long modifiedTime1 = Comparator.readLong(b1, s1 + SCORE_OFFSET + 4 + 4);
            long modifiedTime2 = Comparator.readLong(b2, s2 + SCORE_OFFSET + 4 + 4);
            if (modifiedTime2 != modifiedTime1)
            {
                return (modifiedTime2 - modifiedTime1) > 0 ? 1 : -1;
            }

            return SignatureComparator._compare(b1, SIGNATURE_OFFSET, SIGNATURE_LENGTH, b2, SIGNATURE_OFFSET, SIGNATURE_LENGTH);
        }
    }

    static
    { // register this comparator
        WritableComparator.define(CrawlDatum.class, new Comparator());
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("Version: ").append(CUR_VERSION).append("\n");
        buf.append("Status: ").append(getStatus()).append(" (").append(CrawlDatum.getStatusName(getStatus())).append(")\n");
        buf.append("Fetch time: ").append(new Date(getFetchTime())).append("\n");
        buf.append("Modified time: ").append(new Date(getModifiedTime())).append("\n");
        buf.append("Discovery time: ").append(new Date(getDiscoveryTime())).append("\n");
        buf.append("Retries since fetch: ").append(getRetriesSinceFetch()).append("\n");
        buf.append("Retry interval: ").append(getFetchInterval()).append(" seconds (").append(getFetchInterval() / FetchSchedule.SECONDS_PER_DAY).append(" days)\n");
        buf.append("Score: ").append(getScore()).append("\n");
        buf.append("Score Gravity: ").append(getScoreGravity()).append("\n");
        buf.append("Signature: ").append(getSignature()).append("\n");
        buf.append("Metadata: ");

        if (metadata != null)
        {
            for (Entry<Writable, Writable> e : metadata.entrySet())
            {
                buf.append(e.getKey());
                buf.append(": ");
                buf.append(e.getValue());
            }
        }

        buf.append("\n");

        return buf.toString();
    }

    public String toHtml()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("<li><span class=\"subtitle\">Version:</span> ").append(CUR_VERSION).append("</li>");
        buf.append("<li><span class=\"subtitle\">Status:</span> ").append(getStatus()).append(" (").append(CrawlDatum.getStatusName(getStatus())).append(")</li>");
        buf.append("<li><span class=\"subtitle\">Fetch Time:</span> ").append(new Date(getFetchTime())).append("</li>");
        buf.append("<li><span class=\"subtitle\">Modified Time:</span> ").append(new Date(getModifiedTime())).append("</li>");
        buf.append("<li><span class=\"subtitle\">Discovery Time:</span> ").append(new Date(getDiscoveryTime())).append("</li>");
        buf.append("<li><span class=\"subtitle\">Retries Since Fetch:</span> ").append(getRetriesSinceFetch()).append("</li>");
        buf.append("<li><span class=\"subtitle\">Retry Interval:</span> ").append(getFetchInterval()).append(" seconds (").append(getFetchInterval() / FetchSchedule.SECONDS_PER_DAY).append(" days)</li>");
        buf.append("<li><span class=\"subtitle\">Score:</span> ").append(getScore()).append("</li>");
        buf.append("<li><span class=\"subtitle\">Score Gravity:</span> ").append(getScoreGravity()).append("</li>");
        buf.append("<li><span class=\"subtitle\">Signature:</span> ").append(getSignature()).append("</li>");
        buf.append("<li><span class=\"subtitle\">Metadata:</span> ");

        if (metadata != null)
        {
            for (Entry<Writable, Writable> e : metadata.entrySet())
            {
                buf.append(e.getKey());
                buf.append(": ");
                buf.append(e.getValue());
            }
        }

        buf.append("</li>");

        return buf.toString();
    }

    private boolean metadataEquals(MapWritable otherMetadata)
    {
        if (metadata == null || metadata.isEmpty())
        {
            return otherMetadata == null || otherMetadata.isEmpty();
        }
        if (otherMetadata == null)
        {
            // we already know old the current object is not null or empty
            return false;
        }

        HashSet<Entry<Writable, Writable>> set1 = new HashSet<>(metadata.entrySet());
        HashSet<Entry<Writable, Writable>> set2 = new HashSet<>(otherMetadata.entrySet());

        return set1.equals(set2);
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof CrawlDatum))
        {
            return false;
        }

        CrawlDatum other = (CrawlDatum) o;
        boolean res
                = (status == other.status)
                && (fetchTime == other.fetchTime)
                && (modifiedTime == other.modifiedTime)
                && (discoveryTime == other.discoveryTime)
                && (signature == other.signature)
                && (retries == other.retries)
                && (fetchInterval == other.fetchInterval)
                && (score == other.score)
                && (scoreGravity == other.scoreGravity);

        if (!res)
        {
            return res;
        }

        return metadataEquals(other.metadata);
    }

    @Override
    public int hashCode()
    {
        int res = 0;

        if (metadata != null)
        {
            res ^= metadata.entrySet().hashCode();
        }

        res ^= status;
        res ^= (int) fetchTime;
        res ^= (int) modifiedTime;
        res ^= (int) discoveryTime;
        res ^= (int) signature;
        res ^= retries;
        res ^= fetchInterval;
        res ^= Float.floatToIntBits(score);
        res ^= Float.floatToIntBits(scoreGravity);

        return res;
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
