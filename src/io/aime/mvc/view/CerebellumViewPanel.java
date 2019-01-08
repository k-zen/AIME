package io.aime.mvc.view;

import io.aime.mvc.model.CerebellumGeneralTableModel;
import io.aime.mvc.view.tools.ForLabelTableCellRenderer;
import io.aime.mvc.view.tools.TableColumnAdjuster;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import net.apkc.emma.mvc.AbstractController;
import net.apkc.emma.mvc.AbstractViewPanel;

final class CerebellumViewPanel extends AbstractViewPanel
{

    private CerebellumGeneralTableModel model1;

    final static AbstractViewPanel newBuild()
    {
        return new CerebellumViewPanel();
    }

    private CerebellumViewPanel()
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

        generalTable.setModel(model1 = new CerebellumGeneralTableModel());
        generalTable.setRowHeight(20);
        generalTable.setRowSelectionAllowed(false);
        generalTable.setShowGrid(false);
        generalTable.setGridColor(Color.DARK_GRAY);
        generalTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        TableColumnAdjuster tca1 = new TableColumnAdjuster(generalTable);
        tca1.setColumnHeaderIncluded(true);
        tca1.setColumnDataIncluded(false);
        tca1.adjustColumns();
        generalTable.addMouseListener(new CerebellumViewPanelEvt());
        generalTable.getColumnModel().getColumn(CerebellumGeneralTableModel.HELP_COLUMN_ID).setCellRenderer(ForLabelTableCellRenderer.newBuild());

        return this;
    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent evt)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AbstractController getController()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class CerebellumViewPanelEvt implements MouseListener
    {

        @Override
        public void mouseClicked(MouseEvent e)
        {
            if (e.getSource() == generalTable) {
                if (generalTable.getSelectedColumn() == CerebellumGeneralTableModel.HELP_COLUMN_ID) {
                    if (generalTable.getSelectedRow() == CerebellumGeneralTableModel.CONN_TIME_ROW_ID) {
                        JOptionPane.showMessageDialog(
                                generalTable,
                                "This cell shows the average connection time\n"
                                + "in milliseconds of all nodes connecting to\n"
                                + "the Cerebellum.\n"
                                + "High values (>1000) could mean network problems.",
                                "Help",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        brainImgPanel = new javax.swing.JPanel();
        brainImgLabel = new javax.swing.JLabel();
        containerPanel = new javax.swing.JPanel();
        containerSplitPane = new javax.swing.JSplitPane();
        generalDataPanel = new javax.swing.JPanel();
        generalScrollPane = new javax.swing.JScrollPane();
        generalTable = new javax.swing.JTable();
        unusedPanel = new javax.swing.JPanel();

        setName("Form"); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        brainImgPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        brainImgPanel.setName("brainImgPanel"); // NOI18N

        brainImgLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/img/Cerebellum.png"))); // NOI18N
        brainImgLabel.setName("brainImgLabel"); // NOI18N
        brainImgPanel.add(brainImgLabel);

        add(brainImgPanel);

        containerPanel.setName("containerPanel"); // NOI18N
        containerPanel.setLayout(new javax.swing.BoxLayout(containerPanel, javax.swing.BoxLayout.LINE_AXIS));

        containerSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        containerSplitPane.setResizeWeight(.5d);
        containerSplitPane.setName("containerSplitPane"); // NOI18N

        generalDataPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General Stats"));
        generalDataPanel.setName("generalDataPanel"); // NOI18N
        generalDataPanel.setLayout(new javax.swing.BoxLayout(generalDataPanel, javax.swing.BoxLayout.LINE_AXIS));

        generalScrollPane.setName("generalScrollPane"); // NOI18N

        generalTable.setName("generalTable"); // NOI18N
        generalScrollPane.setViewportView(generalTable);

        generalDataPanel.add(generalScrollPane);

        containerSplitPane.setLeftComponent(generalDataPanel);

        unusedPanel.setName("unusedPanel"); // NOI18N
        unusedPanel.setLayout(new javax.swing.BoxLayout(unusedPanel, javax.swing.BoxLayout.LINE_AXIS));
        containerSplitPane.setRightComponent(unusedPanel);

        containerPanel.add(containerSplitPane);

        add(containerPanel);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel brainImgLabel;
    private javax.swing.JPanel brainImgPanel;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JSplitPane containerSplitPane;
    private javax.swing.JPanel generalDataPanel;
    private javax.swing.JScrollPane generalScrollPane;
    private javax.swing.JTable generalTable;
    private javax.swing.JPanel unusedPanel;
    // End of variables declaration//GEN-END:variables
}
