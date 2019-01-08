package io.aime.mvc.view;

import io.aime.mvc.model.FetcherGeneralStatisticsTableModel;
import io.aime.mvc.model.FetcherProtocolStatusCodesTableModel;
import io.aime.mvc.view.tools.TableColumnAdjuster;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JTable;
import net.apkc.emma.mvc.AbstractController;
import net.apkc.emma.mvc.AbstractViewPanel;

final class FetcherViewPanel extends AbstractViewPanel
{

    private FetcherProtocolStatusCodesTableModel model1;
    private FetcherGeneralStatisticsTableModel model2;

    final static AbstractViewPanel newBuild()
    {
        return new FetcherViewPanel();
    }

    private FetcherViewPanel()
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

        protocolStatusCodesTable.setModel(model1 = new FetcherProtocolStatusCodesTableModel());
        protocolStatusCodesTable.setRowHeight(20);
        protocolStatusCodesTable.setRowSelectionAllowed(false);
        protocolStatusCodesTable.setShowGrid(false);
        protocolStatusCodesTable.setGridColor(Color.DARK_GRAY);
        protocolStatusCodesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        TableColumnAdjuster tca1 = new TableColumnAdjuster(protocolStatusCodesTable);
        tca1.setColumnHeaderIncluded(true);
        tca1.adjustColumns();

        generalStatisticsTable.setModel(model2 = new FetcherGeneralStatisticsTableModel());
        generalStatisticsTable.setRowHeight(20);
        generalStatisticsTable.setRowSelectionAllowed(false);
        generalStatisticsTable.setShowGrid(false);
        generalStatisticsTable.setGridColor(Color.DARK_GRAY);
        generalStatisticsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        TableColumnAdjuster tca2 = new TableColumnAdjuster(generalStatisticsTable);
        tca2.setColumnHeaderIncluded(true);
        tca2.adjustColumns();

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

    private class FetcherViewPanelEvt implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() == resetButton) {
                // TODO
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        actionPanel = new javax.swing.JPanel();
        resetButton = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        mainSplitPane = new javax.swing.JSplitPane();
        statsPanel = new javax.swing.JPanel();
        statsSplitPane = new javax.swing.JSplitPane();
        generalStatisticsPanel = new javax.swing.JPanel();
        generalStatisticsScrollPane = new javax.swing.JScrollPane();
        generalStatisticsTable = new javax.swing.JTable();
        protocolStatusCodesPanel = new javax.swing.JPanel();
        protocolStatusCodesScrollPane = new javax.swing.JScrollPane();
        protocolStatusCodesTable = new javax.swing.JTable();
        graphPanel = new javax.swing.JPanel();
        fetcherStatusCodes = new io.aime.graphs.FetcherStatusCodes();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        actionPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        actionPanel.setName("actionPanel"); // NOI18N
        actionPanel.setLayout(new javax.swing.BoxLayout(actionPanel, javax.swing.BoxLayout.LINE_AXIS));

        resetButton.setText("Reset");
        resetButton.setName("resetButton"); // NOI18N
        resetButton.addActionListener(new FetcherViewPanelEvt());
        actionPanel.add(resetButton);

        add(actionPanel, java.awt.BorderLayout.NORTH);

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new javax.swing.BoxLayout(mainPanel, javax.swing.BoxLayout.Y_AXIS));

        mainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setResizeWeight(.5d);
        mainSplitPane.setName("mainSplitPane"); // NOI18N

        statsPanel.setName("statsPanel"); // NOI18N
        statsPanel.setLayout(new javax.swing.BoxLayout(statsPanel, javax.swing.BoxLayout.LINE_AXIS));

        statsSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        statsSplitPane.setResizeWeight(.5d);
        statsSplitPane.setName("statsSplitPane"); // NOI18N

        generalStatisticsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General Statistics"));
        generalStatisticsPanel.setName("generalStatisticsPanel"); // NOI18N
        generalStatisticsPanel.setLayout(new javax.swing.BoxLayout(generalStatisticsPanel, javax.swing.BoxLayout.LINE_AXIS));

        generalStatisticsScrollPane.setName("generalStatisticsScrollPane"); // NOI18N

        generalStatisticsTable.setName("generalStatisticsTable"); // NOI18N
        generalStatisticsScrollPane.setViewportView(generalStatisticsTable);

        generalStatisticsPanel.add(generalStatisticsScrollPane);

        statsSplitPane.setLeftComponent(generalStatisticsPanel);

        protocolStatusCodesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Protocol Status Codes"));
        protocolStatusCodesPanel.setName("protocolStatusCodesPanel"); // NOI18N
        protocolStatusCodesPanel.setLayout(new javax.swing.BoxLayout(protocolStatusCodesPanel, javax.swing.BoxLayout.LINE_AXIS));

        protocolStatusCodesScrollPane.setName("protocolStatusCodesScrollPane"); // NOI18N

        protocolStatusCodesTable.setName("protocolStatusCodesTable"); // NOI18N
        protocolStatusCodesScrollPane.setViewportView(protocolStatusCodesTable);

        protocolStatusCodesPanel.add(protocolStatusCodesScrollPane);

        statsSplitPane.setRightComponent(protocolStatusCodesPanel);

        statsPanel.add(statsSplitPane);

        mainSplitPane.setLeftComponent(statsPanel);

        graphPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Graph"));
        graphPanel.setName("graphPanel"); // NOI18N
        graphPanel.setLayout(new javax.swing.BoxLayout(graphPanel, javax.swing.BoxLayout.LINE_AXIS));

        fetcherStatusCodes.setName("fetcherStatusCodes"); // NOI18N
        graphPanel.add(fetcherStatusCodes);

        mainSplitPane.setRightComponent(graphPanel);

        mainPanel.add(mainSplitPane);

        add(mainPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionPanel;
    private io.aime.graphs.FetcherStatusCodes fetcherStatusCodes;
    private javax.swing.JPanel generalStatisticsPanel;
    private javax.swing.JScrollPane generalStatisticsScrollPane;
    private javax.swing.JTable generalStatisticsTable;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JPanel protocolStatusCodesPanel;
    private javax.swing.JScrollPane protocolStatusCodesScrollPane;
    private javax.swing.JTable protocolStatusCodesTable;
    private javax.swing.JButton resetButton;
    private javax.swing.JPanel statsPanel;
    private javax.swing.JSplitPane statsSplitPane;
    // End of variables declaration//GEN-END:variables
}
