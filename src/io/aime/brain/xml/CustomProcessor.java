package io.aime.brain.xml;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.apkc.esxp.exceptions.AttributeNotFoundException;
import net.apkc.esxp.exceptions.ParserNotInitializedException;
import net.apkc.esxp.exceptions.TagNotFoundException;
import net.apkc.esxp.processor.Processor;
import net.apkc.esxp.walker.DOMWalkerFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Custom XML processor for the application.
 *
 * @author Andreas P. Koenzen <akc@apkc.net>
 * @see Singleton Pattern
 */
class CustomProcessor
{

    private static final Logger LOG = Logger.getLogger(CustomProcessor.class.getName());
    private static final CustomProcessor _INSTANCE = new CustomProcessor();
    private final byte WALKER = DOMWalkerFactory.STACK_DOM_WALKER;
    private final boolean STRICT_MODE = false;
    private Processor processor = Processor.newBuild();
    private Document doc;
    private NodeList nodes;

    static CustomProcessor getInstance()
    {
        return _INSTANCE;
    }

    private CustomProcessor()
    {
    }

    /**
     * Configure this XML processor.
     *
     * @param xml      The XML document to parse.
     * @param rootNode The root node of the XML.
     *
     * @return This instance.
     */
    CustomProcessor configure(String xml, String rootNode)
    {
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            // Configure to Focus on Content.
            dbFactory.setValidating(false);
            dbFactory.setNamespaceAware(true);
            dbFactory.setCoalescing(true);
            dbFactory.setExpandEntityReferences(true);
            dbFactory.setIgnoringComments(true);
            dbFactory.setIgnoringElementContentWhitespace(true);
            // Create a DOM document.
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler()
            {
                @Override
                public void warning(SAXParseException e) throws SAXException
                {
                    LOG.warn("DOM Warning: " + e.toString(), e);
                }

                @Override
                public void error(SAXParseException e) throws SAXException
                {
                    LOG.error("DOM Error: " + e.toString(), e);
                }

                @Override
                public void fatalError(SAXParseException e) throws SAXException
                {
                    LOG.fatal("DOM Fatal: " + e.toString(), e);
                    throw e;
                }
            });

            doc = builder.parse(new InputSource(new StringReader(xml))); // Create document
            doc.getDocumentElement().normalize(); // Configure
            nodes = doc.getElementsByTagName(rootNode);
        }
        catch (ParserConfigurationException | SAXException | IOException ex)
        {
            LOG.error("Error configurando parseador XML. Error: " + ex.toString(), ex);
            return this;
        }

        return this;
    }

    /**
     * Shortcut method to extract the value of a tag.
     *
     * @param rootNode The root node.
     * @param tag      The name of the tag to extract.
     *
     * @return The value of the tag.
     *
     * @throws ParserNotInitializedException If the processor could not be started.
     * @throws TagNotFoundException          If the tag does not exists.
     */
    String getTagValue(String rootNode, String tag) throws ParserNotInitializedException, TagNotFoundException
    {
        if (nodes == null)
        {
            throw new ParserNotInitializedException("Parser was not started!");
        }

        return processor.searchTagValue(doc, rootNode, tag, STRICT_MODE);
    }

    /**
     * Shortcut method to extract the attribute of a tag.
     *
     * @param rootNode      The root node.
     * @param tag           The name of the tag.
     * @param attributeName The name of the attribute.
     *
     * @return The value of the attribute.
     *
     * @throws ParserNotInitializedException If the processor could not be started.
     * @throws TagNotFoundException          If the tag does not exists.
     * @throws AttributeNotFoundException    If the attribute does not exists.
     */
    String getTagAttribute(String rootNode, String tag, String attributeName) throws ParserNotInitializedException, TagNotFoundException, AttributeNotFoundException
    {
        if (nodes == null)
        {
            throw new ParserNotInitializedException("Parser was not started!");
        }

        return processor.searchTagAttributeValue(doc, rootNode, tag, attributeName, STRICT_MODE);
    }
}
