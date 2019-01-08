package io.aime.util;

// Util
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogWindow {

    private LogHandler lh = new LogHandler();

    public ConcurrentLinkedQueue<LogHandler> getMessages(int queueType) {
        return this.lh.getMessages(queueType);
    }

    public void resetQueue(int queueType) {
        this.lh.resetQueue(queueType);
    }
}
