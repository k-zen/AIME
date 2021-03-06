package io.aime.fetcher;

import io.aime.crawl.CrawlDatum;
import io.aime.protocol.Content;
import io.aime.protocol.Protocol;
import io.aime.protocol.ProtocolFactory;
import io.aime.protocol.ProtocolOutput;
import io.aime.protocol.ProtocolStatus;
import io.aime.protocol.RobotRules;
import java.net.URL;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

/**
 * This class contains utility methods for performing a quick crawl/fetch of a
 * document.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.2
 */
public class QuickFetch
{

    private static final Logger LOG = Logger.getLogger(QuickFetch.class.getName());

    /**
     * Downloads the content of a page or WebService.
     *
     * @param url  The URL where to download.
     * @param conf Configuration's file.
     *
     * @return The contents of the URL.
     */
    public Content quickDocumentFetch(URL url, Configuration conf)
    {
        CrawlDatum datum = new CrawlDatum();

        try
        {
            conf.setBoolean("protocol.plugin.check.robots", false);
            Protocol protocol = new ProtocolFactory(conf).getProtocol(url.toString());
            RobotRules rules = protocol.getRobotRules(new Text(url.toString()), datum);

            if (!rules.isAllowed(url))
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Denied by robots.txt, trying anyway ...");
                }
            }

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Making HTTP request ...");
            }

            ProtocolOutput output = protocol.getProtocolOutput(new Text(url.toString()), datum);
            ProtocolStatus status = output.getStatus();
            Content content = output.getContent();
            switch (status.getCode())
            {
                case ProtocolStatus.SUCCESS: // Busqueda exitosa de pagina. Codigo 200.
                    if (LOG.isDebugEnabled() && content != null && !new String(content.getContent()).equalsIgnoreCase(""))
                    {
                        LOG.debug("Widget downloaded.");
                    }

                    // Volver a su valor original a la directiva de control de robots.txt.
                    conf.setBoolean("protocol.plugin.check.robots", true);

                    return content;
                default:
                    if (LOG.isDebugEnabled() && content != null && !new String(content.getContent()).equalsIgnoreCase(""))
                    {
                        LOG.debug("Widget downloaded.");
                    }

                    // Volver a su valor original a la directiva de control de robots.txt.
                    conf.setBoolean("protocol.plugin.check.robots", true);

                    return content;
            }
        }
        catch (Throwable t)
        {
            LOG.error("Impossible to download widget. Error: " + t.toString(), t);
        }

        return new Content();
    }
}
