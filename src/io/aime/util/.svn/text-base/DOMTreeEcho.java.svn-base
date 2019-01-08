package io.aime.util;

// DOM
import org.w3c.dom.DocumentType;
import org.w3c.dom.Entity;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

// Log4j
import org.apache.log4j.Logger;

/**
 * This class prints an entire DOM Tree to Debug output.
 *
 * @author K-Zen
 */
public class DOMTreeEcho {

    private static final Logger LOG = Logger.getLogger(DOMTreeEcho.class.getName());
    private static final boolean PRINT_WS = false;
    private static final boolean PRINT_ONLY_ELEMENTS = true;
    /**
     * Indent level
     */
    private int indent = 0;
    /**
     * Indentation will be in multiples of basicIndent
     */
    private final String basicIndent = "  ";
    private StringBuffer buffer;

    public DOMTreeEcho() {
        this.buffer = new StringBuffer();
    }

    private void printCommon(Node n) {
        this.buffer.append(" nodeName=\"").append(n.getNodeName()).append("\"");

        String val = n.getNamespaceURI();
        if (val != null) {
            this.buffer.append(" uri=\"").append(val).append("\"");
        }

        val = n.getPrefix();
        if (val != null) {
            this.buffer.append(" pre=\"").append(val).append("\"");
        }

        val = n.getLocalName();
        if (val != null) {
            this.buffer.append(" local=\"").append(val).append("\"");
        }

        val = n.getNodeValue();
        if (val != null) {
            this.buffer.append(" nodeValue=");
            if (val.trim().equals("")) {
                // Whitespace
                this.buffer.append("[WS]");
            }
            else {
                this.buffer.append("\"").append(n.getNodeValue()).append("\"");
            }
        }
    }

    /**
     * Indent to the current level in multiples of basicIndent
     */
    private void outputIndentation() {
        for (int i = 0; i < indent; i++) {
            this.buffer.append(basicIndent);
        }
    }

    /**
     * Recursive routine to print out DOM tree nodes
     *
     * @param n The root node of the tree.
     */
    public void computeTree(Node n) {
        this.outputIndentation();

        int type = n.getNodeType();
        switch (type) {
            case Node.ATTRIBUTE_NODE:
                this.buffer.append("ATTR:");
                this.printCommon(n);
                this.buffer.append("\n");
                break;
            case Node.CDATA_SECTION_NODE:
                this.buffer.append("CDATA:");
                this.printCommon(n);
                this.buffer.append("\n");
                break;
            case Node.COMMENT_NODE:
                this.buffer.append("COMM:");
                this.printCommon(n);
                this.buffer.append("\n");
                break;
            case Node.DOCUMENT_FRAGMENT_NODE:
                this.buffer.append("DOC_FRAG:");
                this.printCommon(n);
                this.buffer.append("\n");
                break;
            case Node.DOCUMENT_NODE:
                this.buffer.append("DOC:");
                this.printCommon(n);
                this.buffer.append("\n");
                break;
            case Node.DOCUMENT_TYPE_NODE:
                this.buffer.append("DOC_TYPE:");
                this.printCommon(n);
                this.buffer.append("\n");

                // Print entities if any
                NamedNodeMap nodeMap = ((DocumentType) n).getEntities();
                indent += 2;
                for (int i = 0; i < nodeMap.getLength(); i++) {
                    Entity entity = (Entity) nodeMap.item(i);
                    this.computeTree(entity);
                }
                indent -= 2;
                break;
            case Node.ELEMENT_NODE:
                this.buffer.append("ELEM:");
                this.printCommon(n);
                this.buffer.append("\n");

                // Print attributes if any.  Note: element attributes are not
                // children of ELEMENT_NODEs but are properties of their
                // associated ELEMENT_NODE.  For this reason, they are printed
                // with 2x the indent level to indicate this.
                NamedNodeMap atts = n.getAttributes();
                indent += 2;
                for (int i = 0; i < atts.getLength(); i++) {
                    Node att = atts.item(i);
                    this.computeTree(att);
                }
                indent -= 2;
                break;
            case Node.ENTITY_NODE:
                this.buffer.append("ENT:");
                this.printCommon(n);
                this.buffer.append("\n");
                break;
            case Node.ENTITY_REFERENCE_NODE:
                this.buffer.append("ENT_REF:");
                this.printCommon(n);
                this.buffer.append("\n");
                break;
            case Node.NOTATION_NODE:
                this.buffer.append("NOTATION:");
                this.printCommon(n);
                this.buffer.append("\n");
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                this.buffer.append("PROC_INST:");
                this.printCommon(n);
                this.buffer.append("\n");
                break;
            case Node.TEXT_NODE:
                this.buffer.append("TEXT:");
                this.printCommon(n);
                this.buffer.append("\n");
                break;
            default:
                this.buffer.append("UNSUPPORTED NODE: ").append(type);
                this.printCommon(n);
                this.buffer.append("\n");
                break;
        }

        // Print children if any
        indent++;
        for (Node child = n.getFirstChild(); child != null; child = child.getNextSibling()) {
            this.computeTree(child);
        }

        indent--;
    }

    public void printTree() {
        LOG.debug(this.buffer.toString());
    }
}
