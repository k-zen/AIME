package io.aime.util;

/**
 * Utility class for Strings.
 *
 * @author K-Zen
 */
public class StringTools {

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Split a string into chunks of MAX bytes.
     *
     * @param TEXT The string to break.
     * @param MAX  Chunk size in bytes.
     *
     * @return An array of strings with the chunks.
     */
    public static String[] splitStringByBytes(final String TEXT, final int MAX) {
        byte[] b = TEXT.getBytes();
        // Check if the TEXT is empty.
        if (b.length == 0) {
            return new String[0];
        }
        // Check how many chunks we need.
        int s = b.length / MAX;
        s = (b.length % MAX > 0) ? s + 1 : s;
        // Asign bytes to chunks.
        String[] chunks = new String[s];
        // Asign chunks up to the last chunk.
        for (int i = 0; i < (s - 1); i++) {
            int offset = i * MAX;
            chunks[i] = new String(b, offset, MAX);
        }
        // Asign the last chunk manually, in order to support flush of leftover bytes.
        chunks[s - 1] = new String(b, (s - 1) * MAX, b.length - ((s - 1) * MAX));

        return chunks;
    }

    /**
     * Split a string into chunks of ≈ size.
     *
     * @param TEXT The string to break.
     * @param MAX  The size in bytes.
     *
     * @return An array of strings with the chunks.
     */
    public static String[] split(final String TEXT, final int MAX) {
        int origLen = TEXT.length();
        int splitNum = origLen / MAX;
        if (origLen % MAX > 0) {
            splitNum += 1;
        }

        String[] splits = new String[splitNum];
        for (int i = 0; i < splitNum; i++) {
            int startPos = i * MAX;
            int endPos = startPos + MAX;
            if (endPos > origLen) {
                endPos = origLen;
            }
            String substr = TEXT.substring(startPos, endPos);
            splits[i] = substr;
        }

        return splits;
    }

    /**
     * Convenience call for {@link #toHexString(byte[], String, int)}, where
     * sep=null; lineLen=Integer.MAX_VALUE.
     */
    public static String toHexString(byte[] buf) {
        return toHexString(buf, null, Integer.MAX_VALUE);
    }

    /**
     * Get a text representation of a byte[] as a hexadecimal String, where each
     * pair of hexadecimal digits corresponds to consecutive bytes in the array.
     *
     * @param buf     Input data
     * @param sep     Separate every pair of hexadecimal digits with this
     *                separator, or null if no separation is needed.
     * @param lineLen Break the output String into lines containing output for
     *                lineLen bytes.
     *
     * @return The hexadecimal string.
     */
    public static String toHexString(byte[] buf, String sep, int lineLen) {
        if (buf == null) {
            return null;
        }

        if (lineLen <= 0) {
            lineLen = Integer.MAX_VALUE;
        }

        StringBuilder res = new StringBuilder(buf.length * 2);
        for (int i = 0; i < buf.length; i++) {
            int b = buf[i];
            res.append(HEX_DIGITS[(b >> 4) & 0xf]);
            res.append(HEX_DIGITS[b & 0xf]);

            if (i > 0 && (i % lineLen) == 0) {
                res.append('\n');
            }
            else if (sep != null && i < lineLen - 1) {
                res.append(sep);
            }
        }

        return res.toString();
    }

    /**
     * Convert a String containing consecutive (no inside whitespace)
     * hexadecimal digits into a corresponding byte array.
     * <p>If the number of digits is not even, a '0' will be appended in the
     * front of the String prior to conversion. Leading and trailing whitespace
     * is ignored.</p>
     *
     * @param text The hexadecimal string.
     *
     * @return Converted byte array, or null if unable to convert.
     */
    public static byte[] fromHexString(String text) {
        text = text.trim();
        if (text.length() % 2 != 0) {
            text = "0" + text;
        }

        int resLen = text.length() / 2;
        int loNibble, hiNibble;
        byte[] res = new byte[resLen];
        for (int i = 0; i < resLen; i++) {
            int j = i << 1;
            hiNibble = charToNibble(text.charAt(j));
            loNibble = charToNibble(text.charAt(j + 1));
            if (loNibble == -1 || hiNibble == -1) {
                return null;
            }

            res[i] = (byte) (hiNibble << 4 | loNibble);
        }

        return res;
    }

    private static int charToNibble(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        else if (c >= 'a' && c <= 'f') {
            return 0xa + (c - 'a');
        }
        else if (c >= 'A' && c <= 'F') {
            return 0xA + (c - 'A');
        }
        else {
            return -1;
        }
    }
}
