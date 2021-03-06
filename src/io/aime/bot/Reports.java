package io.aime.bot;

import io.aime.util.AIMEConstants;
import io.aime.util.LogEventHandler;
import io.aime.util.Mail;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.mail.MessagingException;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

/**
 * This class handles all the reports inside the app.
 *
 * @author Andreas P. Koenzen <akc@apkc.net>
 * @version 0.2
 */
public final class Reports
{

    private static final Logger LOG = Logger.getLogger(Reports.class.getName());

    /**
     * Mail reports.
     *
     * @param conf             Configuration's object
     * @param level            Execution level.
     * @param crawlHome        Crawl home.
     * @param totalTimePartial Execution time.
     * @param totalTimeFull    Total execution time.
     * @param type             1=Parcial 2=Total
     */
    public static void sendMailReport(Configuration conf, int level, String crawlHome, double totalTimePartial, double totalTimeFull, int type)
    {
        StringBuilder report = new StringBuilder();
        report.append("########## Aimebot ").append(AIMEConstants.AIME_VERSION.getStringConstant()).append(" ").append(new SimpleDateFormat("MM/dd/yy").format(new Date())).append(" ##########\n");
        if (type == 1)
        {
            report
                    .append("\n")
                    .append("### CrawlJob Report Level#").append(level + 1).append(":\n")
                    .append("\n")
                    .append("## Time: ").append(new Date().toString())
                    .append("\n\n")
                    .append("## CrawlJob Process Time:\n")
                    .append("+ Seconds: ").append(totalTimePartial).append("\n")
                    .append("+ Minutes: ").append(totalTimePartial / 60).append("\n")
                    .append("+ Hours: ").append(totalTimePartial / 3600).append("\n");
        }
        else if (type == 2)
        {
            report
                    .append("\n")
                    .append("### CrawlJob Report Total:\n")
                    .append("## Time: ").append(new Date().toString()).append("\n")
                    .append("\n\n")
                    .append("## CrawlJob Process Time:\n")
                    .append("+ Seconds: ").append((totalTimeFull)).append("\n")
                    .append("+ Minutes: ").append((totalTimeFull / 60)).append("\n")
                    .append("+ Hours: ").append((totalTimeFull / 3600)).append("\n");
        }

        report.append("##########################################");

        if (type == 1)
        {
            try
            {
                new Mail().sendTextMail(
                        conf.get("aimebot.mail.to", "someone@somewhere.com"),
                        conf.get("aimebot.mail.from", "aimebot@aime.io"),
                        conf.get("aimebot.mail.host", "127.0.0.1"),
                        "Aimebot (CrawlJob) Level#" + (level + 1),
                        report.toString(),
                        5000);
            }
            catch (MessagingException ex)
            {
                String msg = "An error has occur while sending parcial CrawlJob info.";
                LogEventHandler.addNewEvent(new LogEventHandler("Error sending mail.", msg), AIMEConstants.ERROR_EVENT.getIntegerConstant());
                LOG.error(msg, ex);
            }
        }
        else if (type == 2)
        {
            try
            {
                new Mail().sendTextMail(
                        conf.get("aimebot.mail.to", "someone@somewhere.com"),
                        conf.get("aimebot.mail.from", "aimebot@aime.io"),
                        conf.get("aimebot.mail.host", "127.0.0.1"),
                        "Aimebot (CrawlJob) Total",
                        report.toString(),
                        5000);
            }
            catch (MessagingException ex)
            {
                String msg = "An error has occur while sending total CrawlJob info.";
                LogEventHandler.addNewEvent(new LogEventHandler("Error sending mail.", msg), AIMEConstants.ERROR_EVENT.getIntegerConstant());
                LOG.error(msg, ex);
            }
        }
    }
}
