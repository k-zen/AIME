package io.aime.net.protocols;

// Text
import java.text.SimpleDateFormat;
import java.text.ParseException;

// Util
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Class to handle HTTP dates.
 *
 * @author K-Zen
 */
public class HTTPDateFormat {

    protected static final SimpleDateFormat FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    /**
     * HTTP date uses TimeZone GMT
     */
    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Get the HTTP format of the specified date.
     *
     * @param date
     *
     * @return
     */
    public static String toString(Date date) {
        String string;

        synchronized (FORMAT) {
            string = FORMAT.format(date);
        }

        return string;
    }

    public static String toString(Calendar cal) {
        String string;

        synchronized (FORMAT) {
            string = FORMAT.format(cal.getTime());
        }

        return string;
    }

    public static String toString(long time) {
        String string;

        synchronized (FORMAT) {
            string = FORMAT.format(new Date(time));
        }

        return string;
    }

    public static Date toDate(String dateString) throws ParseException {
        Date date;

        synchronized (FORMAT) {
            date = FORMAT.parse(dateString);
        }

        return date;
    }

    public static long toLong(String dateString) throws ParseException {
        long time;

        synchronized (FORMAT) {
            time = FORMAT.parse(dateString).getTime();
        }

        return time;
    }
}
