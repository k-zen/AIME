package io.aime.util;

// Apache Hadoop
import org.apache.hadoop.io.MD5Hash;

// Util
import java.util.Collections;
import java.util.TreeMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class handles all the events that occur within the application.
 *
 * @author K-Zen
 */
public class LogEventHandler {

    private static volatile Map<String, TreeMap<Integer, LogEventHandler>> workingHash = Collections.synchronizedMap(new LinkedHashMap<String, TreeMap<Integer, LogEventHandler>>());
    private static volatile Map<String, TreeMap<Integer, LogEventHandler>> historyHash = Collections.synchronizedMap(new LinkedHashMap<String, TreeMap<Integer, LogEventHandler>>());
    private String title;
    private String eventDescription;
    private long date;

    /**
     * Default constructor.
     */
    private LogEventHandler() {
    }

    /**
     * Builds a new description of the event.
     *
     * @param title            The title of the event.
     * @param eventDescription The description of the events.
     */
    public LogEventHandler(String title, String eventDescription) {
        this.title = title;
        this.eventDescription = eventDescription;
        this.date = System.currentTimeMillis();
    }

    /**
     * Returns the title of a particular event.
     *
     * @return The title of the event.
     */
    private String getEventTitle() {
        return this.title;
    }

    /**
     * Returns the description of a particular event.
     *
     * @return The full description of the event.
     */
    private String getEventDescription() {
        return this.eventDescription;
    }

    /**
     * Returns the date of the event.
     *
     * @return The timestamp of the event.
     */
    private long getEventDate() {
        return this.date;
    }

    /**
     * Adds a new event to the event queue.
     *
     * @param event     The event.
     * @param eventType The type of event.
     */
    public static void addNewEvent(LogEventHandler event, int eventType) {
        // Generate a unique ID for each event, so that no duplicates can occur.
        String uniqueID = MD5Hash.digest(String.valueOf(System.currentTimeMillis())).toString();

        TreeMap<Integer, LogEventHandler> info = new TreeMap<Integer, LogEventHandler>();
        info.put(eventType, event);

        // Put the event in the queue.
        synchronized (workingHash) {
            workingHash.put(uniqueID, info);
        }
        synchronized (historyHash) {
            historyHash.put(uniqueID, info);
        }
    }

    /**
     * Seeks an event and returns it's title.
     *
     * @param id The ID of the event.
     *
     * @return The event's title.
     */
    public static String getEventTitle(String id) {
        synchronized (historyHash) {
            if (!historyHash.isEmpty()) {
                if (historyHash.containsKey(id)) {
                    return historyHash.get(id).firstEntry().getValue().getEventTitle();
                }
                else {
                    return null;
                }
            }
            else {
                return null;
            }
        }
    }

    /**
     * Seeks an event and returns it's description.
     *
     * @param id The ID of the event.
     *
     * @return The event's description.
     */
    public static String getEventDescription(String id) {
        synchronized (historyHash) {
            if (!historyHash.isEmpty()) {
                if (historyHash.containsKey(id)) {
                    return historyHash.get(id).firstEntry().getValue().getEventDescription();
                }
                else {
                    return null;
                }
            }
            else {
                return null;
            }
        }
    }

    /**
     * Seeks an event and returns it's date.
     *
     * @param id The ID of the event.
     *
     * @return The event's date.
     */
    public static long getEventDate(String id) {
        synchronized (historyHash) {
            if (!historyHash.isEmpty()) {
                if (historyHash.containsKey(id)) {
                    return historyHash.get(id).firstEntry().getValue().getEventDate();
                }
                else {
                    return 0L;
                }
            }
            else {
                return 0L;
            }
        }
    }

    /**
     * Seeks an event and returns it's severity.
     *
     * @param id The ID of the event.
     *
     * @return The event's severity.
     */
    public static int getEventSeverity(String id) {
        synchronized (historyHash) {
            if (!historyHash.isEmpty()) {
                if (historyHash.containsKey(id)) {
                    return historyHash.get(id).firstEntry().getKey();
                }
                else {
                    return 0;
                }
            }
            else {
                return 0;
            }
        }
    }

    /**
     * Returns all the events in storage up to this moment.
     *
     * @return The events current database.
     */
    public static Map<String, TreeMap<Integer, LogEventHandler>> getAllEventMessages() {
        return workingHash;
    }

    /**
     * Resets the temporary events queue.
     */
    public static void resetEventTempQueue() {
        synchronized (workingHash) {
            if (!workingHash.isEmpty()) {
                workingHash.clear();
            }
        }
    }
}
