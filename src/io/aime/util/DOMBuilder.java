package io.aime.util;

// DOM
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.CDATASection;

// SAX
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.ext.LexicalHandler;

// Util
import java.util.Stack;

/**
 * This class takes SAX events (in addition to some extra events that SAX
 * doesn't handle yet) and adds the result to a document or document fragment.
 *
 * @author Nutch.org
 * @author K-Zen
 */
public class DOMBuilder implements ContentHandler, LexicalHandler {

    /**
     * Root document
     */
    public Document mDoc;
    /**
     * Current node
     */
    protected Node mCurrentNode = null;
    /**
     * First node of document fragment or null if not a DocumentFragment
     */
    public DocumentFragment mDocFrag = null;
    /**
     * Vector of element nodes
     */
    protected Stack<Element> mElemStack = new Stack<Element>();
    /**
     * Flag indicating that we are processing a CData section.
     */
    protected boolean mInCData = false;

    /**
     * DOM_Builder instance constructor... it will add the DOM nodes to the
     * document fragment.
     *
     * @param doc  Root document
     * @param node Current node
     */
    public DOMBuilder(Document doc, Node node) {
        mDoc = doc;
        mCurrentNode = node;
    }

    /**
     * DOM_Builder instance constructor... it will add the DOM nodes to the
     * document fragment.
     *
     * @param doc     Root document
     * @param docFrag Document fragment
     */
    public DOMBuilder(Document doc, DocumentFragment docFrag) {
        mDoc = doc;
        mDocFrag = docFrag;
    }

    /**
     * DOM_Builder instance constructor... it will add the DOM nodes to the
     * document.
     *
     * @param doc Root document
     */
    public DOMBuilder(Document doc) {
        mDoc = doc;
    }

    /**
     * Get the root node of the DOM being created. This is either a Document or
     * a DocumentFragment.
     *
     * @return The root document or document fragment if not null
     */
    public Node getRootNode() {
        return (null != mDocFrag) ? (Node) mDocFrag : (Node) mDoc;
    }

    /**
     * Get the node currently being processed.
     *
     * @return the current node being processed
     */
    public Node getCurrentNode() {
        return this.mCurrentNode;
    }

    /**
     * Return null since there is no Writer for this class.
     *
     * @return null
     */
    public java.io.Writer getWriter() {
        return null;
    }

    /**
     * Append a node to the current container.
     *
     * @param newNode New node to append
     *
     * @throws org.xml.sax.SAXException
     */
    protected void append(Node newNode) throws org.xml.sax.SAXException {
        Node currentNode = this.mCurrentNode;

        if (null != currentNode) {
            currentNode.appendChild(newNode);
        }
        else if (null != mDocFrag) {
            mDocFrag.appendChild(newNode);
        }
        else {
            boolean ok = true;
            short type = newNode.getNodeType();

            if (type == Node.TEXT_NODE) {
                String data = newNode.getNodeValue();

                if ((null != data) && (data.trim().length() > 0)) {
                    throw new org.xml.sax.SAXException("Warning: can't output text before document element!  Ignoring...");
                }

                ok = false;
            }
            else if (type == Node.ELEMENT_NODE) {
                if (mDoc.getDocumentElement() != null) {
                    throw new org.xml.sax.SAXException("Can't have more than one root on a DOM!");
                }
            }

            if (ok) {
                mDoc.appendChild(newNode);
            }
        }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
    }

    @Override
    public void startDocument() throws org.xml.sax.SAXException {
    }

    @Override
    public void endDocument() throws org.xml.sax.SAXException {
    }

    @Override
    public void startElement(String ns, String localName, String name, Attributes atts) throws org.xml.sax.SAXException {
        Element elem;

        // Note that the namespace-aware call must be used to correctly
        // construct a Level 2 DOM, even for non-namespaced nodes.
        if ((null == ns) || (ns.length() == 0)) {
            elem = mDoc.createElementNS(null, name);
        }
        else {
            elem = mDoc.createElementNS(ns, name);
        }

        this.append(elem);

        try {
            int nAtts = atts.getLength();

            if (0 != nAtts) {
                for (int i = 0; i < nAtts; i++) {

                    //System.out.println("type " + atts.getType(i) + " name " + atts.getLocalName(i) );
                    // First handle a possible ID attribute
                    if (atts.getType(i).equalsIgnoreCase("ID")) {
                        this.setIDAttribute(atts.getValue(i), elem);
                    }

                    String attrNS = atts.getURI(i);

                    if ("".equals(attrNS)) {
                        attrNS = null; // DOM represents no-namespace as null
                    }
                    // System.out.println("attrNS: "+attrNS+", localName: "+atts.getQName(i)
                    //                   +", qname: "+atts.getQName(i)+", value: "+atts.getValue(i));
                    // Crimson won't let us set an xmlns: attribute on the DOM.
                    String attrQName = atts.getQName(i);

                    // In SAX, xmlns: attributes have an empty namespace, while in DOM they should have the xmlns namespace
                    if (attrQName.startsWith("xmlns:")) {
                        attrNS = "http://www.w3.org/2000/xmlns/";
                    }

                    // ALWAYS use the DOM Level 2 call!
                    elem.setAttributeNS(attrNS, attrQName, atts.getValue(i));
                }
            }

            this.mElemStack.push(elem);
            this.mCurrentNode = elem;
        }
        catch (java.lang.Exception de) {
            throw new org.xml.sax.SAXException(de);
        }
    }

    @Override
    public void endElement(String ns, String localName, String name) throws org.xml.sax.SAXException {
        mElemStack.pop();
        mCurrentNode = mElemStack.isEmpty() ? null : (Node) mElemStack.peek();
    }

    /**
     * Set an ID string to node association in the ID table.
     *
     * @param id   The ID string.
     * @param elem The associated ID.
     */
    public void setIDAttribute(String id, Element elem) {
    }

    @Override
    public void characters(char ch[], int start, int length) throws org.xml.sax.SAXException {
        if (this.isOutsideDocElem() && XMLCharacterRecognizer.isWhiteSpace(ch, start, length)) {
            return;  // avoid DOM006 Hierarchy request error
        }
        if (mInCData) {
            this.cdata(ch, start, length);

            return;
        }

        String s = new String(ch, start, length);
        Node childNode;
        childNode = mCurrentNode != null ? mCurrentNode.getLastChild() : null;
        if (childNode != null && childNode.getNodeType() == Node.TEXT_NODE) {
            ((Text) childNode).appendData(s);
        }
        else {
            Text text = mDoc.createTextNode(s);
            this.append(text);
        }
    }

    /**
     * If available, when the disable-output-escaping attribute is used, output
     * raw text without escaping. A PI will be inserted in front of the node
     * with the name "lotusxsl-next-is-raw" and a value of "formatter-to-dom".
     *
     * @param ch     Array containing the characters
     * @param start  Index to start of characters in the array
     * @param length Number of characters in the array
     *
     * @throws org.xml.sax.SAXException
     */
    public void charactersRaw(char ch[], int start, int length) throws org.xml.sax.SAXException {
        if (this.isOutsideDocElem() && XMLCharacterRecognizer.isWhiteSpace(ch, start, length)) {
            return;  // avoid DOM006 Hierarchy request error
        }

        String s = new String(ch, start, length);
        this.append(mDoc.createProcessingInstruction("xslt-next-is-raw", "formatter-to-dom"));
        this.append(mDoc.createTextNode(s));
    }

    @Override
    public void startEntity(String name) throws org.xml.sax.SAXException {
    }

    @Override
    public void endEntity(String name) throws org.xml.sax.SAXException {
    }

    /**
     * Receive notivication of a entity_reference.
     *
     * @param name name of the entity reference
     *
     * @throws org.xml.sax.SAXException
     */
    public void entityReference(String name) throws org.xml.sax.SAXException {
        this.append(mDoc.createEntityReference(name));
    }

    @Override
    public void ignorableWhitespace(char ch[], int start, int length) throws org.xml.sax.SAXException {
        if (this.isOutsideDocElem()) {
            return;  // avoid DOM006 Hierarchy request error
        }

        String s = new String(ch, start, length);
        this.append(mDoc.createTextNode(s));
    }

    /**
     * Tell if the current node is outside the document element.
     *
     * @return true if the current node is outside the document element.
     */
    private boolean isOutsideDocElem() {
        return (null == mDocFrag) && mElemStack.size() == 0 && (null == mCurrentNode || mCurrentNode.getNodeType() == Node.DOCUMENT_NODE);
    }

    @Override
    public void processingInstruction(String target, String data) throws org.xml.sax.SAXException {
        this.append(mDoc.createProcessingInstruction(target, data));
    }

    @Override
    public void comment(char ch[], int start, int length) throws org.xml.sax.SAXException {
        // tagsoup sometimes submits invalid values here
        if (ch == null || start < 0 || length >= (ch.length - start) || length < 0) {
            return;
        }

        this.append(mDoc.createComment(new String(ch, start, length)));
    }

    @Override
    public void startCDATA() throws org.xml.sax.SAXException {
        this.mInCData = true;
        this.append(mDoc.createCDATASection(""));
    }

    @Override
    public void endCDATA() throws org.xml.sax.SAXException {
        this.mInCData = false;
    }

    /**
     * Receive notification of cdata.
     *
     * <p>The Parser will call this method to report each chunk of character
     * data. SAX parsers may return all contiguous character data in a single
     * chunk, or they may split it into several chunks; however, all of the
     * characters in any single event must come from the same external entity,
     * so that the Locator provides useful information.</p>
     *
     * <p>The application must not attempt to read from the array outside of the
     * specified range. Note that some parsers will report whitespace using the
     * ignorableWhitespace() method rather than this one (validating parsers
     * must do so).</p>
     *
     * @param ch     The characters from the XML document.
     * @param start  The start position in the array.
     * @param length The number of characters to read from the array.
     *
     * @throws org.xml.sax.SAXException
     *
     * @see #ignorableWhitespace
     * @see org.xml.sax.Locator
     */
    public void cdata(char ch[], int start, int length) throws org.xml.sax.SAXException {
        if (this.isOutsideDocElem() && XMLCharacterRecognizer.isWhiteSpace(ch, start, length)) {
            return;  // avoid DOM006 Hierarchy request error
        }

        String s = new String(ch, start, length);
        // XXX ab@apache.org: modified from the original, to accomodate TagSoup. 
        Node n = this.mCurrentNode.getLastChild();

        if (n instanceof CDATASection) {
            ((CDATASection) n).appendData(s);
        }
        else if (n instanceof Comment) {
            ((Comment) n).appendData(s);
        }
    }

    @Override
    public void startDTD(String name, String publicId, String systemId) throws org.xml.sax.SAXException {
    }

    @Override
    public void endDTD() throws org.xml.sax.SAXException {
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws org.xml.sax.SAXException {
    }

    @Override
    public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException {
    }

    @Override
    public void skippedEntity(String name) throws org.xml.sax.SAXException {
    }
}
