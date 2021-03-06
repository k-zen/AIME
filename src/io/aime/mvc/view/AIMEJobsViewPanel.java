package io.aime.mvc.view;

import io.aime.mvc.model.AIMEJobsTreeModel;
import java.beans.PropertyChangeEvent;
import net.apkc.emma.mvc.AbstractController;
import net.apkc.emma.mvc.AbstractViewPanel;
import net.apkc.emma.utils.Fonts;

final class AIMEJobsViewPanel extends AbstractViewPanel
{

    private AIMEJobsTreeModel model;

    final static AbstractViewPanel newBuild()
    {
        return new AIMEJobsViewPanel();
    }

    private AIMEJobsViewPanel()
    {
        createComponent().configure(null).markVisibility(true);
    }

    @Override
    protected AbstractViewPanel createComponent()
    {
        initComponents();
        return this;
    }

    @Override
    public AbstractViewPanel configure(Object o)
    {
        // ALLOWED ACTIONS
        // CONTROLLERS
        // CONFIGURE MODELS
        // CONFIGURE VIEWS

        // jobsTree.setModel(model);
        return this;
    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent evt)
    {
        switch (evt.getPropertyName())
        {
        }
    }

    @Override
    public AbstractController getController()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        treePanel = new javax.swing.JPanel();
        jobsScrollPane = new javax.swing.JScrollPane();
        jobsTree = new javax.swing.JTree();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6), javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Jobs:", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, Fonts.getInstance().getDefaultBold(14)
        )));
        setName("Form"); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        treePanel.setName("treePanel"); // NOI18N
        treePanel.setLayout(new javax.swing.BoxLayout(treePanel, javax.swing.BoxLayout.LINE_AXIS));

        jobsScrollPane.setName("jobsScrollPane"); // NOI18N

        jobsTree.setName("jobsTree"); // NOI18N
        jobsScrollPane.setViewportView(jobsTree);

        treePanel.add(jobsScrollPane);

        add(treePanel);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jobsScrollPane;
    private javax.swing.JTree jobsTree;
    private javax.swing.JPanel treePanel;
    // End of variables declaration//GEN-END:variables
}
