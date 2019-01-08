package io.aime.brain;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;

/**
 *
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 */
public class BrainTaskQueue
{

    private static final Logger LOG = Logger.getLogger(BrainTaskQueue.class.getName());
    private static final BrainTaskQueue _INSTANCE = new BrainTaskQueue();
    private final Queue<Object> TASKS = new ConcurrentLinkedQueue<>();

    private BrainTaskQueue()
    {
    }

    public static BrainTaskQueue getInstance()
    {
        return _INSTANCE;
    }

}
