package io.aime.util;

// Log4j
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Appender for the LogWindow view of the application.
 *
 * <p>This LogWindow shows everything that's happening inside the application in
 * real-time.</p>
 *
 * @author K-Zen
 */
public class LogWindowAppender extends AppenderSkeleton {

    private LogHandler log = new LogHandler();

    @Override
    protected synchronized void append(LoggingEvent event) {
        this.log.addNewLog(new LogHandler(event.getLevel().toInt(), ((String) event.getMessage())));
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }
}
