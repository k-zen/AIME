package io.aime.util;

/**
 * Class to handle digits and numbers.
 *
 * @author K-Zen
 */
public class DigitsTools {

    /**
     * Method to format a size in bytes to any corresponding unit of metric.
     *
     * @param bytes The amount of bytes.
     * @param si    If the Metric System should be applied. i.e. kB over KiB.
     *
     * @return A formatted string with the size.
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;

        if (bytes < unit) {
            return bytes + " B";
        }

        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");

        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
