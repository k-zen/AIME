package io.aime.mvc.view;

import io.aime.mvc.model.AIMEEventsTableModel;
import io.aime.mvc.view.tools.ForLabelTableCellRenderer;
import io.aime.mvc.view.tools.TableColumnAdjuster;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import javax.swing.JTable;
import net.apkc.emma.mvc.AbstractController;
import net.apkc.emma.mvc.AbstractViewPanel;
import net.apkc.emma.utils.Fonts;

final class AIMEEventsViewPanel extends AbstractViewPanel
{

    private AIMEEventsTableModel model;

    final static AbstractViewPanel newBuild()
    {
        return new AIMEEventsViewPanel();
    }

    private AIMEEventsViewPanel()
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

        eventsTable.setModel(model = new AIMEEventsTableModel());
        eventsTable.setRowHeight(20);
        eventsTable.setRowSelectionAllowed(false);
        eventsTable.setShowGrid(false);
        eventsTable.setGridColor(Color.DARK_GRAY);
        eventsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        TableColumnAdjuster tca = new TableColumnAdjuster(eventsTable);
        tca.setDynamicAdjustment(true);
        tca.adjustColumns();

        eventsTable.getColumnModel().getColumn(AIMEEventsTableModel.STATUS_COLUMN_ID).setCellRenderer(ForLabelTableCellRenderer.newBuild());

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

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        eventsScrollPane = new javax.swing.JScrollPane();
        eventsTable = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6), javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Events:", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, Fonts.getInstance().getDefaultBold(14)
        )));
        setName("Form"); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        eventsScrollPane.setAutoscrolls(true);
        eventsScrollPane.setName("eventsScrollPane"); // NOI18N

        eventsTable.setName("eventsTable"); // NOI18N
        eventsTable.setShowGrid(true);
        eventsScrollPane.setViewportView(eventsTable);

        add(eventsScrollPane);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane eventsScrollPane;
    private javax.swing.JTable eventsTable;
    // End of variables declaration//GEN-END:variables
}
