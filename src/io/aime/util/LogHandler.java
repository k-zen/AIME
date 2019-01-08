package io.aime.util;

// Log4j
import org.apache.log4j.Priority;

// Util
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class handles all the logging events in the application.
 *
 * <p>For instance the LogWindow appender generates a logging event, and inputs
 * that event into this class, for later viewing through the LogWindow
 * window.</p>
 *
 * @author K-Zen
 */
public class LogHandler {

    private static final int QUEUE_LIMIT = 100;
    private static volatile ConcurrentLinkedQueue<LogHandler> in = new ConcurrentLinkedQueue<LogHandler>(); // Info queue
    private static volatile ConcurrentLinkedQueue<LogHandler> er = new ConcurrentLinkedQueue<LogHandler>(); // Error queue
    private static volatile ConcurrentLinkedQueue<LogHandler> fa = new ConcurrentLinkedQueue<LogHandler>(); // Fatal queue
    private static volatile ConcurrentLinkedQueue<LogHandler> wa = new ConcurrentLinkedQueue<LogHandler>(); // Warning queue
    private int logLevel;
    private String logDescription;

    /**
     * Default constructor.
     */
    public LogHandler() {
    }

    /**
     * Builds a new log description.
     *
     * @param logLevel       The priority level.
     * @param logDescription The description of the logging event.
     */
    public LogHandler(int logLevel, String logDescription) {
        this.logLevel = logLevel;
        this.logDescription = logDescription;
    }

    /**
     * Returns the level of the log event.
     *
     * @return The priority level.
     */
    public int getLogLevel() {
        return this.logLevel;
    }

    /**
     * Returns the description of the log event.
     *
     * @return The full description of the logging event.
     */
    public String getLogDescription() {
        return this.logDescription;
    }

    /**
     * Add a new log event to the queue.
     *
     * <p>The queue will be filled up until the limit has been reached, when
     * this happens the queue will be automatically emptied.</p>
     *
     * @param log The logging message.
     */
    public void addNewLog(LogHandler log) {
        switch (log.getLogLevel()) {
            case Priority.INFO_INT:
                synchronized (LogHandler.in) {
                    if (LogHandler.in.size() > LogHandler.QUEUE_LIMIT) {
                        LogHandler.in.clear();
                    }

                    LogHandler.in.add(log);
                }
                break;
            case Priority.ERROR_INT:
                synchronized (LogHandler.er) {
                    if (LogHandler.er.size() > LogHandler.QUEUE_LIMIT) {
                        LogHandler.er.clear();
                    }

                    LogHandler.er.add(log);
                }
                break;
            case Priority.FATAL_INT:
                synchronized (LogHandler.fa) {
                    if (LogHandler.fa.size() > LogHandler.QUEUE_LIMIT) {
                        LogHandler.fa.clear();
                    }

                    LogHandler.fa.add(log);
                }
                break;
            case Priority.WARN_INT:
                synchronized (LogHandler.wa) {
                    if (LogHandler.wa.size() > LogHandler.QUEUE_LIMIT) {
                        LogHandler.wa.clear();
                    }

                    LogHandler.wa.add(log);
                }
                break;
        }
    }

    public ConcurrentLinkedQueue<LogHandler> getMessages(int queueType) {
        switch (queueType) {
            case Priority.INFO_INT:
                return LogHandler.in;
            case Priority.ERROR_INT:
                return LogHandler.er;
            case Priority.FATAL_INT:
                return LogHandler.fa;
            case Priority.WARN_INT:
                return LogHandler.wa;
            default:
                return LogHandler.in;
        }
    }

    public void resetQueue(int queueType) {
        switch (queueType) {
            case Priority.INFO_INT:
                synchronized (LogHandler.in) {
                    if (!LogHandler.in.isEmpty()) {
                        LogHandler.in.clear();
                    }
                }
                break;
            case Priority.ERROR_INT:
                synchronized (LogHandler.er) {
                    if (!LogHandler.er.isEmpty()) {
                        LogHandler.er.clear();
                    }
                }
                break;
            case Priority.FATAL_INT:
                synchronized (LogHandler.fa) {
                    if (!LogHandler.fa.isEmpty()) {
                        LogHandler.fa.clear();
                    }
                }
                break;
            case Priority.WARN_INT:
                synchronized (LogHandler.wa) {
                    if (!LogHandler.wa.isEmpty()) {
                        LogHandler.wa.clear();
                    }
                }
                break;
        }
    }
}
