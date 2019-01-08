package io.aime.brain.xml;

import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.BrainXMLData.Parameter;
import io.aime.util.Timer;
import net.apkc.esxp.exceptions.AttributeNotFoundException;
import net.apkc.esxp.exceptions.ParserNotInitializedException;
import net.apkc.esxp.exceptions.TagNotFoundException;
import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

public final class Handler
{

    private static final Logger LOG = Logger.getLogger(Handler.class.getName());
    private CustomProcessor processor = CustomProcessor.getInstance();

    private Handler()
    {
    }

    public static Handler newBuild()
    {
        return new Handler();
    }

    public static Text makeXMLRequest(BrainXMLData data)
    {
        Timer timer = new Timer();
        timer.starTimer();

        String xml = String.format(
                ""
                + "<?xml version=\"1.0\"?>"
                + "<cerebellum job=\"%s\">"
                + "<class>%s</class>"
                + "<function>%s</function>"
                + "<param type=\"%s\">%s</param>"
                + "</cerebellum>",
                data.getJob(),
                data.getClazz().getName(),
                data.getFunction(),
                data.getParam().getType().getName(),
                new String(Base64.encodeBase64(data.getParam().getDataAsByteArray())));

        timer.endTimer();

        if (LOG.isInfoEnabled()) {
            LOG.info("Tiempo Marshall: " + timer.computeOperationTime(Timer.Time.MILLISECOND) + "ms");
        }

        return new Text(xml);
    }

    public BrainXMLData getBrainXMLData(String xml) throws ClassNotFoundException
    {
        Timer timer = new Timer();
        timer.starTimer();

        // Re-start the parser and point to root node.
        processor = processor.configure(xml, "cerebellum");

        try {
            byte job = Byte.parseByte(processor.getTagAttribute("cerebellum", "cerebellum", "job"));
            Class clazz = Class.forName(processor.getTagValue("cerebellum", "class"));
            String function = processor.getTagValue("cerebellum", "function");
            Parameter param = Parameter.newBuild().setType(Class.forName(processor.getTagAttribute("cerebellum", "param", "type"))).setDataAsByteArray(Base64.decodeBase64(processor.getTagValue("cerebellum", "param")));

            BrainXMLData data = BrainXMLData.newBuild().setJob(job).setClazz(clazz).setFunction(function).setParam(param);

            timer.endTimer();

            if (LOG.isInfoEnabled()) {
                LOG.info("Tiempo Unmarshall: " + timer.computeOperationTime(Timer.Time.MILLISECOND) + "ms");
            }

            return data;
        }
        catch (ParserNotInitializedException | TagNotFoundException | AttributeNotFoundException e) {
            LOG.fatal("Error procesando XML. Error: " + e.toString(), e);
            return BrainXMLData.newBuild();
        }
    }
}
