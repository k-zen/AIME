package io.aime.util;

import io.aime.bot.Alerts;
import javax.swing.SwingWorker;
import org.apache.hadoop.conf.Configuration;

/**
 * This class checks the results of each process and acts accordingly.
 *
 * @author K-Zen
 */
public class ProcessKiller
{

    /**
     * Checks the exit code of a process, if it's 0 do nothing, if >0 means
     * error and the admin should be notified.
     *
     * @param exitCode    The code returned by the event
     * @param processName The name of the process
     * @param runner      The thread used to launch the process
     * @param conf        Configuration's object
     *
     * @return An integer denoting the state of the process.
     */
    public static int checkExitCode(int exitCode, String processName, SwingWorker<?, ?> runner, Configuration conf)
    {
        if (exitCode != 0)
        {
            String t = "Process " + processName + " concluded incorrectly.";
            String e = "The process \"" + processName + "\" has concluded incorrectly, check the logs for more information.\nThe overall process was halted.";
            LogEventHandler.addNewEvent(new LogEventHandler(t, e), AIMEConstants.ERROR_EVENT.getIntegerConstant());
            Alerts.abruptTermination(conf); // Send alert mail.
        }

        return exitCode;
    }
}
