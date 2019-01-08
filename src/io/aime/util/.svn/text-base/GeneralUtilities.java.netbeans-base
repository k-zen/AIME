package io.aime.util;

// Apache Commons
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

// IO
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

// Log4j
import org.apache.log4j.Logger;

// Text
import java.text.DecimalFormat;

// Util
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities class.
 *
 * @author K-Zen
 */
public class GeneralUtilities {

    private static final Logger LOG = Logger.getLogger(GeneralUtilities.class.getName());

    /**
     * This method sorts out a HashMap by value.
     *
     * @param passedMap The HashMap to sort out.
     *
     * @return The sorted HashMap.
     */
    public static final LinkedHashMap<String, Integer> sortMapByValuesD(HashMap<String, Integer> passedMap) {
        List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
        List<Integer> mapValues = new ArrayList<Integer>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        Collections.reverse(mapValues);
        Collections.reverse(mapKeys);

        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();

        Iterator<Integer> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key.toString()).toString();
                String comp2 = val.toString();

                if (comp1.equals(comp2)) {
                    passedMap.remove(key.toString());
                    mapKeys.remove(key.toString());
                    sortedMap.put((String) key, (Integer) val);

                    break;
                }
            }
        }

        return sortedMap;
    }

    /**
     * Reads a file line by line and stores its contents into a HashMap, denoted
     * by &lt;lineNumber,line&gt;.
     *
     * @param path        The absolute path of the file.
     * @param exitIfEmpty If the method should abort when there are no lines.
     *
     * @return A HashMap with all the file's lines.
     */
    public static final HashMap<Integer, String> readFileByLine(String path, boolean exitIfEmpty) {
        HashMap<Integer, String> lines = new HashMap<Integer, String>();
        File f = new File(path);
        BufferedReader in = null;

        if (f.length() == 0 && exitIfEmpty) {
            LOG.error("Empty file: " + f.getAbsolutePath());

            return null;
        }
        else {
            try {
                in = new BufferedReader(new FileReader(f));
                String line;

                int i = 1;
                while ((line = in.readLine()) != null) {
                    lines.put(i, line);
                    i++;
                }
            }
            catch (IOException e) {
                LOG.fatal("Error trying to read file's contents: " + f.getAbsolutePath(), e);
            }
            finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                }
                catch (IOException e) {
                    LOG.fatal("Error trying to close InputStream to file: " + f.getAbsolutePath(), e);
                }
            }
        }

        return lines;
    }

    /**
     * This method reads a file line by line and uses regex to filter the lines
     * that it should store.
     *
     * @param path        The absolute path of the file.
     * @param exitIfEmpty If the method should abort when there are no lines.
     * @param regex       The regex used to filter lines.
     *
     * @return A HashMap with all the lines.
     */
    public static final HashMap<String, Integer> readFileByLineRegex(String path, boolean exitIfEmpty, String regex) {
        HashMap<String, Integer> lines = new HashMap<String, Integer>();
        File f = new File(path);
        BufferedReader in = null;

        if (f.length() == 0 && exitIfEmpty) {
            LOG.error("Empty file: " + f.getAbsolutePath());

            return null;
        }
        else {
            try {
                in = new BufferedReader(new FileReader(f));
                String line;

                Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.COMMENTS | Pattern.UNICODE_CASE);

                while ((line = in.readLine()) != null) {
                    Matcher m = p.matcher(line);

                    if (m.find()) {
                        lines.put(line, 1);
                    }
                    else {
                        continue;
                    }
                }
            }
            catch (IOException e) {
                LOG.fatal("Error trying to read the file's contents: " + f.getAbsolutePath(), e);
            }
            finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                }
                catch (IOException e) {
                    LOG.fatal("Error trying to close InputStream to file: " + f.getAbsolutePath(), e);
                }
            }
        }

        return lines;
    }

    /**
     * This method reads a file line by line and returns a List.
     *
     * @param path        The absolute path of the file.
     * @param exitIfEmpty If the method should abort when there are no lines.
     *
     * @return A List with all the lines.
     */
    public static final List<String> readFileByLineList(String path, boolean exitIfEmpty) {
        List<String> lines = new ArrayList<String>();
        File f = new File(path);
        BufferedReader in = null;

        if (f.length() == 0 && exitIfEmpty) {
            LOG.error("Empty file: " + f.getAbsolutePath());

            return null;
        }
        else {
            try {
                in = new BufferedReader(new FileReader(f));
                String line;

                while ((line = in.readLine()) != null) {
                    lines.add(line);
                }
            }
            catch (IOException e) {
                LOG.fatal("Error trying to read the file's contents: " + f.getAbsolutePath(), e);
            }
            finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                }
                catch (IOException e) {
                    LOG.fatal("Error trying to close InputStream to file: " + f.getAbsolutePath(), e);
                }
            }
        }

        return lines;
    }

    /**
     * This method reads a stream and creates a List.
     *
     * @param stream      The file's stream.
     * @param exitIfEmpty If the method should abort when there are no lines.
     *
     * @return A List with all the lines.
     */
    public static final List<String> readFileByLineList(InputStream stream, boolean exitIfEmpty) {
        List<String> lines = new ArrayList<String>();
        BufferedReader in = null;
        File fTmp = null;

        try {
            fTmp = File.createTempFile("temporary-file", ".tmp");
            fTmp.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(fTmp);
            byte[] buf = new byte[256];
            int read = 0;

            while ((read = stream.read(buf)) > 0) {
                fos.write(buf, 0, read);
            }

            fos.close();
        }
        catch (IOException e) {
            LOG.fatal("Error trying to write InputStream to temporary file: " + fTmp.getAbsolutePath(), e);
        }

        if (fTmp.length() == 0 && exitIfEmpty) {
            LOG.error("Empty file: " + fTmp.getAbsolutePath());

            return null;
        }
        else {
            try {
                in = new BufferedReader(new FileReader(fTmp));
                String line;

                while ((line = in.readLine()) != null) {
                    lines.add(line);
                }
            }
            catch (IOException e) {
                LOG.fatal("Error trying to read file's contents: " + fTmp.getAbsolutePath(), e);
            }
            finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                }
                catch (IOException e) {
                    LOG.fatal("Error trying to close InputStream to file: " + fTmp.getAbsolutePath(), e);
                }
            }
        }

        return lines;
    }

    /**
     * This method writes a message to a file.
     *
     * @param path    The absolute path to the file.
     * @param message The string to write.
     */
    public static final void logToFile(String path, String message) {
        if (path == null || path.isEmpty()) {
            LOG.error("Passed path is empty!");

            return;
        }

        try {
            PrintWriter out = new PrintWriter(new FileWriter(path, true));
            out.println(message);
            out.close();
        }
        catch (IOException e) {
            LOG.fatal("Error trying to write string to file: " + path, e);
        }
    }

    /**
     * Creates a listing of the entire directory and its contents.
     *
     * @param directory The parent directory.
     *
     * @return A list containing the entire directory listing.
     *
     * @throws IOException
     */
    public static final List<String> directoryListing(File directory) throws IOException {
        Stack<String> stack = new Stack<String>();
        List<String> list = new ArrayList<String>();

        if (directory.isFile()) {
            if (directory.canRead()) {
                list.add(directory.getName());
            }

            return list;
        }

        String root = directory.getParent();
        stack.push(directory.getName());

        while (!stack.empty()) {
            String current = stack.pop();
            File curDir = new File(root, current);
            String[] fileList = curDir.list();

            if (fileList != null) {
                for (String entry : fileList) {
                    File f = new File(curDir, entry);

                    if (f.isFile()) {
                        if (f.canRead()) {
                            list.add(current + File.separator + entry);
                        }
                        else {
                            throw new IOException("Can't read file: " + f.getPath());
                        }
                    }
                    else if (f.isDirectory()) {
                        list.add(current + File.separator + entry);
                        stack.push(current + File.separator + f.getName());
                    }
                    else {
                        throw new IOException("Unknown entry: " + f.getPath());
                    }
                }
            }
        }

        return list;
    }

    /**
     * This method checks to see if a dir exists.
     *
     * @param path Path to dir.
     *
     * @return boolean TRUE if exists, FALSE otherwise.
     */
    public static final boolean directoryExists(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        File dir = new File(path);

        if (dir.exists() && dir.isDirectory()) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * This method checks to see if a dir is empty.
     *
     * @param path Path to dir.
     *
     * @return TRUE if dir is empty, FALSE otherwise.
     */
    public static final boolean directoryIsEmpty(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        File dir = new File(path);
        String[] files = dir.list();

        if (files == null || files.length == 0) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Deletes a directory and its contents.
     *
     * @param path      Path to dir.
     * @param recursive If the deletion should be recursive.
     *
     * @return TRUE if the dir has been deleted, FALSE otherwise.
     */
    public static final boolean deleteDirectory(String path, boolean recursive) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        File f = new File(path);

        if (recursive) {
            if (f.exists()) {
                File[] files = f.listFiles();

                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        GeneralUtilities.deleteDirectory(files[i].getPath(), true);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }

        return (f.delete());
    }

    /**
     * Deletes only the contents of the folder, but leaves the parent intact.
     *
     * @param path      Path to dir.
     * @param recursive If the deletion should be recursive.
     *
     * @return TRUE if the contents has been deleted correctly, FALSE otherwise.
     */
    public static final boolean deleteDirectoryContents(String path, boolean recursive) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        File f = new File(path);

        if (recursive) {
            if (f.exists()) {
                File[] files = f.listFiles();

                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        GeneralUtilities.deleteDirectory(files[i].getPath(), true);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }

        return true;
    }

    /**
     * Checks if a dir is writable.
     *
     * @param path Path to dir.
     *
     * @return TRUE if the dir is writable, FALSE otherwise.
     */
    public static final boolean directoryCanWrite(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        return new File(path).canWrite();
    }

    /**
     * Writes a message/string to standard out.
     *
     * @param aMessage The message/string.
     */
    public static final void logToConsole(String aMessage) {
        if (!aMessage.equals("") && aMessage != null) {
            try {
                PrintStream out = new PrintStream(System.out, true, "UTF-8");
                out.println(aMessage);
            }
            catch (UnsupportedEncodingException e) {
                LOG.fatal("Unsupported character set: UTF-8.", e);
            }
        }
    }

    /**
     * Appends an empty line to standard out.
     */
    public static final void addLineToConsole() {
        System.out.println("");
    }

    /**
     * This method rounds up a decimal number up to N decimals. i.e.. 4,5687 ->
     * 4,56
     *
     * @param d The decimal number.
     *
     * @return The rounded number.
     */
    public static final Double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");

        return Double.valueOf(twoDForm.format(d));
    }

    /**
     * This method converts a stream into a string.
     *
     * @param is The input stream.
     *
     * @return The resulting string.
     */
    public static final String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            if (reader.readLine() != null) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            }
        }
        catch (IOException e) {
            LOG.fatal("Generic error. Error: " + e.toString(), e);
        }
        finally {
            try {
                is.close();
            }
            catch (Exception e) {
                LOG.fatal("Impossible to close stream. Error: " + e.toString(), e);
            }
        }

        return StringUtils.chomp(sb.toString());
    }

    /**
     * This method returns the last modified dir in a given dir.
     *
     * @param dir The path to dir.
     *
     * @return The last modified file.
     */
    public static final File lastFileModified(String dir) {
        if (dir == null || dir.isEmpty()) {
            return null;
        }

        File fl = new File(dir);
        File[] files = fl.listFiles(
                new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        long lastMod = Long.MIN_VALUE;
        File choice = null;

        for (File file : files) {
            if (file.lastModified() > lastMod) {
                choice = file;
                lastMod = file.lastModified();
            }
        }

        return choice;
    }

    /**
     * Checks if an ENV variable is set.
     *
     * @param var The name of the variable.
     *
     * @return TRUE if set, FALSE otherwise.
     */
    public static final boolean checkEnvironmentVariables(String var) {
        if (System.getenv(var) == null || System.getenv(var).equals("")) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Computes the difference between 2 times or timestamps, and returns the
     * result in readable format.
     *
     * <p>
     * Usage:</br>
     * <ul>
     * <li>s = seconds</li>
     * <li>m = minutes</li>
     * <li>h = hours</li>
     * <li>d = days</li>
     * <li>l | null = milliseconds</li>
     * </ul>
     * </p>
     *
     * @param startTime Starting time.
     * @param endTime   Ending time.
     * @param unit      Timeunit.
     * @param round     If we should round the value.
     *
     * @return The time difference between the two times, being the end time
     *         always bigger.
     */
    public static final double computeOperationTime(long startTime, long endTime, String unit, boolean round) {
        if (unit.equalsIgnoreCase("s")) {
            return (round) ? (double) Math.round((endTime - startTime) / 1000.00f) : ((endTime - startTime) / 1000.00f);
        }

        if (unit.equalsIgnoreCase("m")) {
            return (round) ? (double) Math.round(((endTime - startTime) / 1000.00f) / 60.00f) : (((endTime - startTime) / 1000.00f) / 60.00f);
        }

        if (unit.equalsIgnoreCase("h")) {
            return (round) ? (double) Math.round(((endTime - startTime) / 1000.00f) / 3600.00f) : (((endTime - startTime) / 1000.00f) / 3600.00f);
        }

        if (unit.equalsIgnoreCase("d")) {
            return (round) ? (double) Math.round(((endTime - startTime) / 1000.00f) / 86400.00f) : (((endTime - startTime) / 1000.00f) / 86400.00f);
        }

        return (double) (endTime - startTime);
    }

    /**
     * Returns the JVM memory consumption.
     *
     * @param onlyUse If TRUE, only show consuption, if FALSE show consumption
     *                and execute GC.
     *
     * @return The memory consumption in kilobytes (KB).
     */
    public static final int getMemoryUse(boolean onlyUse) {
        if (!onlyUse) {
            GeneralUtilities.putOutGC();
        }

        long totalMemory = Runtime.getRuntime().totalMemory();

        if (!onlyUse) {
            GeneralUtilities.putOutGC();
        }

        long freeMemory = Runtime.getRuntime().freeMemory();

        return (int) ((totalMemory - freeMemory) / 1024) / 1024;
    }

    /**
     * Calls the GC.
     */
    public static final void putOutGC() {
        GeneralUtilities.collectGarbage();
        GeneralUtilities.collectGarbage();
    }

    /**
     * Forces the GC.
     */
    public static final void collectGarbage() {
        long SLEEP_INTERVAL = 100;

        try {
            System.gc();
            Thread.sleep(SLEEP_INTERVAL);
            System.runFinalization();
            Thread.sleep(SLEEP_INTERVAL);
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Trims a URL for showing.
     *
     * @param url The URL.
     * @param l   The desired long.
     *
     * @return The trimmed URL.
     */
    public static final String trimURL(String url, int l) {
        int length = (l == 0) ? 80 : l;

        if (url.length() > length) {
            url = StringUtils.left(url, length) + "...";
        }
        else {
            url = StringUtils.left(url, length);
        }

        return url;
    }

    /**
     * Breaks a String for showing. Breaking a String its just inserting a new
     * line character inside the text at a specified position, in order for the
     * string to spawn 2 lines instead of just 1 very long line.
     *
     * @param string  The String.
     * @param l       The desired long.
     * @param newLine The newline char, can be: \n or $lt;br/&gt;
     *
     * @return The trimmed URL.
     */
    public static final String breakString(String string, int l, String newLine) {
        int length = (l == 0) ? 80 : l;
        int inserts = string.length() / length;
        StringBuilder b = new StringBuilder(string);

        for (int k = 0; k < inserts; k++) {
            b.insert(length * (k + 1), newLine);
        }

        return b.toString();
    }

    /**
     * This method wraps long words inside a paragraph.
     *
     * @param string  The String.
     * @param l       The desired long.
     * @param newLine The newline char, can be: \n or $lt;br/&gt;
     *
     * @return The original paragraph with all long words wrapped up.
     */
    public static final String wrapWordsInParagraph(String string, int l, String newLine) {
        StringTokenizer tokens = new StringTokenizer(string);
        StringBuilder buffer = new StringBuilder();
        while (tokens.hasMoreTokens()) {
            buffer.append(GeneralUtilities.breakString(tokens.nextToken(), l, newLine)).append(" ");
        }

        return buffer.toString().trim();
    }

    /**
     * Trims a paragraph into N words. It can be used as a summarizer.
     *
     * @param text        Text to trim.
     * @param separator   Separator of words.
     * @param l           Length of words to include in the response.
     * @param addEllipsis If it should add an ellipsis.
     *
     * @return The trimmed text.
     */
    public static final String trimText(String text, String separator, int l, boolean addEllipsis) {
        if (text.isEmpty()) {
            return text;
        }

        String[] textArray = text.split(separator);

        return (addEllipsis) ? StringUtils.join(ArrayUtils.subarray(textArray, 0, l), " ").concat(" ...") : StringUtils.join(ArrayUtils.subarray(textArray, 0, l), " ");
    }

    /**
     * This method ensures that the output String has only valid XML unicode
     * characters as specified by the XML 1.0 standard.
     *
     * <p>For reference, please see <a
     * href=”http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char”>the
     * standard</a>. This method will return an empty String if the input is
     * null or empty.</p>
     *
     * @param in The String whose non-valid characters we want to remove.
     *
     * @return The in String, stripped of non-valid characters.
     */
    public static final String stripNonValidXMLCharacters(String in) {
        StringBuilder out = new StringBuilder(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) {
            return "";
        }

        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i);
            if ((current == 0x9) || (current == 0xA) || (current == 0xD) || ((current >= 0x20) && (current <= 0xD7FF)) || ((current >= 0xE000) && (current <= 0xFFFD)) || ((current >= 0x10000) && (current <= 0x10FFFF))) {
                out.append(current);
            }
        }

        return out.toString();
    }

    /**
     * Computes the total size of a given folder. #TODO: Add support from DFS.
     *
     * @param filePaths The path to the directory.
     *
     * @return A formatted string containing the size.
     */
    public static String getFolderSize(String[] filePaths) {
        long size = 0L;

        for (String file : filePaths) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            File f = new File(file);
            if (!f.exists()) {
                continue;
            }

            try {
                size += FileUtils.sizeOfDirectory(f);
            }
            catch (IllegalArgumentException e) {
                // This means the file isn't there anymore, just fail silently.
            }
        }

        return DigitsTools.humanReadableByteCount(size, false);
    }
}
