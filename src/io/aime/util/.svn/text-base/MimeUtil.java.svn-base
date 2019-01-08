package io.aime.util;

// Apache Hadoop
import org.apache.hadoop.conf.Configuration;

// IO
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

// Log4j
import org.apache.log4j.Logger;

// Tika
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

// Util
import java.util.HashMap;
import java.util.Map;

/**
 * This is a facade class to insulate AIME from its underlying Mime Type
 * substrate library, <a href="http://incubator.apache.org/tika/">Apache
 * Tika</a>.
 *
 * <p>Any mime handling code should be placed in this utility class, and hidden
 * from the AIME classes that rely on it.</p>
 *
 * @author Nutch.org
 * @author K-Zen
 */
public final class MimeUtil {

    private static final Logger LOG = Logger.getLogger(MimeUtil.class.getName());
    private static final String SEPARATOR = ";";
    private static Map<String, String> mimeTypeMap = new HashMap<String, String>(1);
    private MimeTypes mimeTypes;
    private Tika tika;
    private boolean mimeMagic;

    static {
        BufferedReader reader = null;
        try {
            reader = new LineNumberReader(new InputStreamReader(MimeUtil.class.getResourceAsStream("/mime.types")));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\=");
                MimeUtil.mimeTypeMap.put(parts[0], parts[1]);
            }
        }
        catch (Exception e) {
            LOG.error("Impossible to read mime types file. Error: " + e.toString(), e);
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (Exception e) {
                    LOG.error("Error closing down mime type reader. Error: " + e.toString());
                }
            }
        }
    }

    public MimeUtil() {
        this.mimeTypes = new MimeTypes();
        this.mimeMagic = false;
    }

    public MimeUtil(Configuration conf) {
        this.tika = new Tika();

        ObjectCache objectCache = ObjectCache.get(conf);
        MimeTypes mimeTypez = (MimeTypes) objectCache.getObject(MimeTypes.class.getName());

        if (mimeTypez == null) {
            mimeTypez = MimeTypes.getDefaultMimeTypes();
            objectCache.setObject(MimeTypes.class.getName(), mimeTypez);
        }

        this.mimeTypes = mimeTypez;
        this.mimeMagic = conf.getBoolean("mime.type.magic", true);
    }

    /**
     * This method will return the associated extension with a particular
     * MimeType.
     *
     * <p>Warning: This method might return more than one extension for a given
     * MimeType. i.e. The mime type text/plain contains several extensions like:
     * txt, conf, etc.</p>
     *
     * @param mimeType The full mime type.
     *
     * @return The extension associated.
     */
    public static String getExtension(String mimeType) {
        String def = "unknown";

        if (!MimeUtil.mimeTypeMap.isEmpty()) {
            return (MimeUtil.mimeTypeMap.get(mimeType) != null) ? MimeUtil.mimeTypeMap.get(mimeType) : def;
        }

        return def;
    }

    /**
     * Cleans a {@link MimeType} name by removing out the actual
     * {@link MimeType}, from a string of the form:
     *
     * <pre>&lt;primary type&gt;/&lt;sub type&gt; ; &lt; optional params</pre>
     *
     * @param origType he original mime type string to be cleaned.
     *
     * @return The primary type, and subtype, concatenated, e.g., the actual
     *         mime type.
     */
    public static String cleanMimeType(String origType) {
        if (origType == null) {
            return null;
        }

        // take the origType and split it on ';'
        String[] tokenizedMimeType = origType.split(SEPARATOR);
        if (tokenizedMimeType.length > 1) {
            // there was a ';' in there, take the first value
            return tokenizedMimeType[0];
        }
        else {
            // there wasn't a ';', so just return the orig type
            return origType;
        }
    }

    /**
     * A facade interface to trying all the possible mime type resolution
     * strategies available within Tika.
     *
     * <p>First, the mime type provided in
     * <code>typeName</code> is cleaned, with {@link #cleanMimeType(String)}.
     * Then the cleaned mime type is looked up in the underlying Tika
     * {@link MimeTypes} registry, by its cleaned name. If the {@link MimeType}
     * is found, then that mime type is used, otherwise URL resolution is used
     * to try and determine the mime type. If that means is unsuccessful, and if
     * <code>mime.type.magic</code> is enabled in {@link NutchConfiguration},
     * then mime type magic resolution is used to try and obtain a
     * better-than-the-default approximation of the {@link MimeType}.</p>
     *
     * @param typeName The original mime type, returned from a
     *                 {@link ProtocolOutput}.
     * @param url      The given
     * @param data     The byte data, returned from the crawl, if any.
     *
     * @see url, that Nutch was trying to crawl.
     *
     * @return The correctly, automatically guessed {@link MimeType} name.
     */
    public String autoResolveContentType(String typeName, String url, byte[] data) {
        String retType = null;
        String magicType = null;
        MimeType type = null;
        String cleanedMimeType = null;

        try {
            cleanedMimeType = MimeUtil.cleanMimeType(typeName) != null ? this.mimeTypes.forName(MimeUtil.cleanMimeType(typeName)).getName() : null;
        }
        catch (MimeTypeException mte) {
            // Seems to be a malformed mime type name...
        }

        // first try to get the type from the cleaned type name
        try {
            type = cleanedMimeType != null ? this.mimeTypes.forName(cleanedMimeType) : null;
        }
        catch (MimeTypeException e) {
            type = null;
        }

        // if returned null, or if it's the default type then try url resolution
        if (type == null || (type != null && type.getName().equals(MimeTypes.OCTET_STREAM))) {
            // If no mime-type header, or cannot find a corresponding registered
            // mime-type, then guess a mime-type from the url pattern
            type = this.mimeTypes.getMimeType(url) != null ? this.mimeTypes.getMimeType(url) : type;
        }

        retType = type.getName();

        // if magic is enabled use mime magic to guess if the mime type returned
        // from the magic guess is different than the one that's already set so far
        // if it is, and it's not the default mime type, then go with the mime type
        // returned by the magic
        if (this.mimeMagic) {
            magicType = tika.detect(data);

            // Deprecated in Tika 1.0 See https://issues.apache.org/jira/browse/NUTCH-1230
            // MimeType magicType = this.mimeTypes.getMimeType(data);
            if (magicType != null && !magicType.equals(MimeTypes.OCTET_STREAM) && !magicType.equals(MimeTypes.PLAIN_TEXT) && retType != null && !retType.equals(magicType)) {
                // If magic enabled and the current mime type differs from that of the
                // one returned from the magic, take the magic mimeType
                retType = magicType;
            }

            // if type is STILL null after all the resolution strategies, go for the
            // default type
            if (retType == null) {
                try {
                    retType = MimeTypes.OCTET_STREAM;
                }
                catch (Exception e) {
                    // Ignore
                }
            }
        }

        return retType;
    }

    /**
     * Facade interface to Tika's underlying
     * {@link MimeTypes#getMimeType(String)} method.
     *
     * @param url A string representation of the document {@link URL} to sense
     *            the {@link MimeType} for.
     *
     * @return An appropriate {@link MimeType}, identified from the given
     *         Document url in string form.
     */
    public String getMimeType(String url) {
        return tika.detect(url);
    }

    /**
     * A facade interface to Tika's underlying {@link MimeTypes#forName(String)}
     * method.
     *
     * @param name The name of a valid {@link MimeType} in the Tika mime
     *             registry.
     *
     * @return The object representation of the {@link MimeType}, if it exists,
     *         or null otherwise.
     */
    public String forName(String name) {
        try {
            return this.mimeTypes.forName(name).toString();
        }
        catch (MimeTypeException e) {
            LOG.error("Caugh exception when detecting MimeType by name: [" + name + "]. Error: " + e.toString(), e);

            return null;
        }
    }

    /**
     * Facade interface to Tika's underlying {@link MimeTypes#getMimeType(File)}
     * method.
     *
     * @param f The {@link File} to sense the {@link MimeType} for.
     *
     * @return The {@link MimeType} of the given {@link File}, or null if it
     *         cannot be determined.
     */
    public String getMimeType(File f) {
        try {
            return tika.detect(f);
        }
        catch (Exception e) {
            LOG.error("Exception getting MimeType for file: [" + f.getPath() + "]. Error: " + e.toString(), e);

            return null;
        }
    }

    /**
     * Facade interface to Tika's underlying {@link MimeTypes#getMimeType(File)}
     * method.
     *
     * @param s The stream of bytes from the file.
     *
     * @return The MimeType of the given stream, or null if it cannot be
     *         determined.
     */
    public String getMimeType(InputStream s) {
        String type = null;

        try {
            type = tika.detect(s);
        }
        catch (Exception e) {
            LOG.error("Exception getting MimeType for stream. Error: " + e.toString(), e);
        }
        finally {
            try {
                s.close();
            }
            catch (IOException e) {
                LOG.error("Error closing down stream from MimeType detection. Error: " + e.toString(), e);
            }
        }

        return type;
    }
}
