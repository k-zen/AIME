package io.aime.util;

// IO
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// Log4j
import org.apache.log4j.Logger;

// Util
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * A collection of utility methods for working on GZIPed data.
 *
 * <p>
 * Also there are methods to compress and uncompress directories using the
 * ZIP compression algorithm.</p>
 *
 * @author Nutch.org
 * @author K-Zen
 */
public class GZIPUtils {

    private static final Logger LOG = Logger.getLogger(GZIPUtils.class.getName());
    private static final int EXPECTED_COMPRESSION_RATIO = 5;
    private static final int BUF_SIZE = 4096;

    /**
     * Returns an gunzipped copy of the input array. If the gzipped input has
     * been truncated or corrupted, a best-effort attempt is made to unzip as
     * much as possible.
     *
     * <p>
     * If no data can be extracted
     * <code>null</code> is returned.</p>
     *
     * @param in
     *
     * @return
     */
    public static final byte[] unzipBestEffort(byte[] in) {
        return unzipBestEffort(in, Integer.MAX_VALUE);
    }

    /**
     * Returns an gunzipped copy of the input array, truncated to
     * <code>sizeLimit</code> bytes, if necessary. If the gzipped input has been
     * truncated or corrupted, a best-effort attempt is made to unzip as much as
     * possible.
     *
     * <p>
     * If no data can be extracted
     * <code>null</code> is returned.</p>
     *
     * @param in
     * @param sizeLimit
     *
     * @return
     */
    public static final byte[] unzipBestEffort(byte[] in, int sizeLimit) {
        try {
            // decompress using GZIPInputStream 
            ByteArrayOutputStream outStream = new ByteArrayOutputStream(EXPECTED_COMPRESSION_RATIO * in.length);
            GZIPInputStream inStream = new GZIPInputStream(new ByteArrayInputStream(in));

            byte[] buf = new byte[BUF_SIZE];
            int written = 0;
            while (true) {
                try {
                    int size = inStream.read(buf);
                    if (size <= 0) {
                        break;
                    }
                    if ((written + size) > sizeLimit) {
                        outStream.write(buf, 0, sizeLimit - written);
                        break;
                    }
                    outStream.write(buf, 0, size);
                    written += size;
                }
                catch (Exception e) {
                    break;
                }
            }
            try {
                outStream.close();
            }
            catch (IOException e) {
            }

            return outStream.toByteArray();

        }
        catch (IOException e) {
            return null;
        }
    }

    /**
     * Returns an gunzipped copy of the input array.
     *
     * @param in
     *
     * @return
     *
     * @throws IOException
     */
    public static final byte[] unzip(byte[] in) throws IOException {
        // decompress using GZIPInputStream 
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(EXPECTED_COMPRESSION_RATIO * in.length);
        GZIPInputStream inStream = new GZIPInputStream(new ByteArrayInputStream(in));

        byte[] buf = new byte[BUF_SIZE];
        while (true) {
            int size = inStream.read(buf);
            if (size <= 0) {
                break;
            }
            outStream.write(buf, 0, size);
        }
        outStream.close();

        return outStream.toByteArray();
    }

    /**
     * Returns an gzipped copy of the input array.
     *
     * @param in
     *
     * @return
     */
    public static final byte[] zip(byte[] in) {
        try {
            // compress using GZIPOutputStream 
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream(in.length / EXPECTED_COMPRESSION_RATIO);
            GZIPOutputStream outStream = new GZIPOutputStream(byteOut);

            try {
                outStream.write(in);
            }
            catch (Exception e) {
                LOG.warn(e.getMessage(), e);
            }

            try {
                outStream.close();
            }
            catch (IOException e) {
                LOG.warn(e.getMessage(), e);
            }

            return byteOut.toByteArray();

        }
        catch (IOException e) {
            LOG.warn(e.getMessage(), e);

            return null;
        }
    }

    /**
     * This method creates a ZIP file from a directory and all its contents.
     *
     * @param srcDir The parent directory or source dir.
     * @param out    The output stream where to write.
     *
     * @throws IOException
     */
    public static void zip(File srcDir, OutputStream out) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Compressing segment for transfering to SegmentServer.");
        }

        List<String> fileList = GeneralUtilities.directoryListing(srcDir);
        ZipOutputStream zout = new ZipOutputStream(out);
        zout.setLevel(9);
        zout.setComment("AIME ZIP Maker v0.2");

        for (String fileName : fileList) {
            File file = new File(srcDir.getParent(), fileName);
            String zipName = fileName;
            ZipEntry ze;

            if (file.isFile()) {
                ze = new ZipEntry(zipName);
                ze.setTime(file.lastModified());
                zout.putNextEntry(ze);
                FileInputStream fin = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int readed = 0;

                while ((readed = fin.read(buffer)) > 0) {
                    zout.write(buffer, 0, readed);
                    zout.flush();
                }

                fin.close();
            }
            else {
                ze = new ZipEntry(zipName + "/");
                ze.setTime(file.lastModified());
                zout.putNextEntry(ze);
            }
        }

        zout.close();
        out.close();
    }

    /**
     * This class uncompresses a ZIP file to a particular directory.
     *
     * @param zipfile   The path to the ZIP file.
     * @param directory The directory where to uncompress.
     *
     * @throws IOException
     */
    public static void unzip(File zipfile, File directory) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Decompressing segment for usage in SegmentServer.");
        }

        ZipFile zip = new ZipFile(zipfile);
        Enumeration<? extends ZipEntry> entries = zip.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File file = new File(directory, entry.getName());

            if (entry.isDirectory()) {
                file.mkdirs();
            }
            else {
                file.getParentFile().mkdirs();

                InputStream in = zip.getInputStream(entry);
                OutputStream out = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int readed = 0;

                while ((readed = in.read(buffer)) > 0) {
                    out.write(buffer, 0, readed);
                    out.flush();
                }

                out.close();
                in.close();
            }
        }

        zip.close();
    }
}
