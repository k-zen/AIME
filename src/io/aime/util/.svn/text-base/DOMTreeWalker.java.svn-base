package io.aime.util;

// DOM
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// Util
import java.util.Stack;

/**
 * A utility class that allows the walking of any DOM tree using a stack instead
 * of recursion.
 *
 * <p>As the node tree is walked the next node is popped off of the stack and
 * all of its children are automatically added to the stack to be called in tree
 * order.</p>
 *
 * <p>Currently this class is not thread safe. It is assumed that only one
 * thread will be accessing the
 * <code>DOM_Tree_Walker</code> at any given time.</p>
 *
 * @author Nutch.org
 * @author K-Zen
 */
public class DOMTreeWalker {

    private Node currentNode;
    private NodeList currentChildren;
    private Stack<Node> nodes = new Stack<Node>();

    /**
     * Starts the
     * <code>Node</code> tree from the root node.
     *
     * @param rootNode The root node. The head of the tree.
     */
    public DOMTreeWalker(Node rootNode) {
        this.nodes.add(rootNode);
    }

    /**
     * Returns the next
     * <code>Node</code> on the stack and pushes all of its children onto the
     * stack, allowing us to walk the node tree without the use of recursion. If
     * there are no more nodes on the stack then null is returned.
     *
     * @return Node The next <code>Node</code> on the stack or null if there
     *         isn't a next node.
     */
    public Node nextNode() {
        if (!hasNext()) {
            return null;
        }

        this.currentNode = this.nodes.pop();
        this.currentChildren = this.currentNode.getChildNodes();
        int childLen = (this.currentChildren != null) ? this.currentChildren.getLength() : 0;

        /*
         * Put the children node on the stack in first to last order.
         */
        for (int i = childLen - 1; i >= 0; i--) {
            this.nodes.add(this.currentChildren.item(i));
        }

        return currentNode;
    }

    /**
     * Skips over and removes from the node stack the children of the last node.
     * When getting a next node from the walker, that node's children are
     * automatically added to the stack. You can call this method to remove
     * those children from the stack.
     *
     * <p>This is useful when you don't want to process deeper into the current
     * path of the node tree but you want to continue processing sibling
     * nodes.</p>
     */
    public void skipChildren() {
        int childLen = (this.currentChildren != null) ? this.currentChildren.getLength() : 0;

        for (int i = 0; i < childLen; i++) {
            Node child = this.nodes.peek();
            if (child.equals(this.currentChildren.item(i))) {
                this.nodes.pop();
            }
        }
    }

    /**
     * Returns true if there are more nodes on the current stack.
     *
     * @return TRUE if there are more nodes, FALSE otherwise.
     */
    public boolean hasNext() {
        return (this.nodes.size() > 0);
    }
}
