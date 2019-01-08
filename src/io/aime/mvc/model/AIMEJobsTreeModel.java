package io.aime.mvc.model;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Model for tree of AIME jobs. This tree will be an entry
 * point to manage all AIME jobs.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 */
public final class AIMEJobsTreeModel implements TreeModel
{

    @Override
    public Object getRoot()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getChild(Object parent, int index)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getChildCount(Object parent)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isLeaf(Object node)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getIndexOfChild(Object parent, Object child)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addTreeModelListener(TreeModelListener l)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
