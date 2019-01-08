package io.aime.bot;

import io.aime.util.GeneralUtilities;
import io.aime.util.Mail;
import javax.mail.MessagingException;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

/**
 * This class handles all the alert messages inside the app.
 *
 * @author Andreas P. Koenzen <akc@apkc.net>
 * @version 0.2
 */
public final class Alerts
{

    private static final Logger LOG = Logger.getLogger(Alerts.class.getName());

    private Alerts()
    {
    }

    /**
     * Sends an abrupt termination alert.
     *
     * @param conf Configuration's file.
     */
    public static void abruptTermination(Configuration conf)
    {
        StringBuilder report = new StringBuilder();
        report
                .append("\n")
                .append("### Aimebot Alerts:\n")
                .append("+ Message: The search finished abruptly.\n")
                .append("+ Memory Consumption: ").append(GeneralUtilities.getMemoryUse(true)).append("MB \n");
        try
        {
            new Mail().sendTextMail(
                    conf.get("aimebot.mail.to", "someone@somewhere.com"),
                    conf.get("aimebot.mail.from", "aimebot@aime.io"),
                    conf.get("aimebot.mail.host", "127.0.0.1"),
                    "Aimebot - ERROR!",
                    report.toString(),
                    5000);
        }
        catch (MessagingException e)
        {
            LOG.error("An error has ocurred sending mail. Error: " + e.toString(), e);
        }
    }

    /**
     * Sends a warning message.
     *
     * @param conf    Configuration's file.
     * @param message The message to send.
     */
    public static void sendWarning(Configuration conf, String message)
    {
        StringBuilder report = new StringBuilder();
        report
                .append("\n")
                .append("### Aimebot Alerts:\n")
                .append("+ Message: ").append(message).append("\n")
                .append("+ Memory Consumption: ").append(GeneralUtilities.getMemoryUse(true)).append("MB \n");
        try
        {
            new Mail().sendTextMail(
                    conf.get("aimebot.mail.to", "someone@somewhere.com"),
                    conf.get("aimebot.mail.from", "aimebot@aime.io"),
                    conf.get("aimebot.mail.host", "127.0.0.1"),
                    "Aimebot - WARNING!",
                    report.toString(),
                    5000);
        }
        catch (MessagingException e)
        {
            LOG.error("An error has ocurred sending mail. Error: " + e.toString(), e);
        }
    }
}
