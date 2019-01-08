package io.aime.mvc.view;

import io.aime.brain.Brain;
import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.MetadataConsole;
import io.aime.brain.xml.Handler;
import io.aime.mvc.controller.MainController;
import io.aime.mvc.model.AIMEBotConsoleTableModel;
import io.aime.mvc.model.MainModel;
import io.aime.mvc.view.tools.ForLabelTableCellRenderer;
import io.aime.mvc.view.tools.TableColumnAdjuster;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JTable;
import javax.swing.Timer;
import net.apkc.emma.mvc.AbstractController;
import net.apkc.emma.mvc.AbstractViewPanel;

final class AIMEBotConsoleViewPanel extends AbstractViewPanel
{

    private MainController controller;
    private AIMEBotConsoleTableModel model;

    final static AbstractViewPanel newBuild()
    {
        return new AIMEBotConsoleViewPanel();
    }

    private AIMEBotConsoleViewPanel()
    {
        createComponent().configure(null).markVisibility(true);
    }

    @Override
    public AbstractViewPanel createComponent()
    {
        initComponents();
        return this;
    }

    @Override
    public AbstractViewPanel configure(Object o)
    {
        // ALLOWED ACTIONS
        // CONTROLLERS
        controller = new MainController();
        // CONFIGURE MODELS
        controller.addModel(new MainModel());
        // CONFIGURE VIEWS
        controller.addView(this);

        botConsoleTable.setModel(model = new AIMEBotConsoleTableModel());
        botConsoleTable.setRowHeight(20);
        botConsoleTable.setRowSelectionAllowed(false);
        botConsoleTable.setShowGrid(false);
        botConsoleTable.setGridColor(Color.DARK_GRAY);
        botConsoleTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        TableColumnAdjuster tca = new TableColumnAdjuster(botConsoleTable);
        tca.setDynamicAdjustment(true);
        tca.adjustColumns();

        botConsoleTable.getColumnModel().getColumn(AIMEBotConsoleTableModel.TYPE_COLUMN_ID).setCellRenderer(ForLabelTableCellRenderer.newBuild());
        new Timer(100, new DataListener()).start();

        return this;
    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals(MainController.BOT_CONSOLE_QUEUE_DEEP_TEXT_PROPERTY)) {
            String newStringValue = evt.getNewValue().toString();

            if (!queueSizeCountLabel.getText().equals(newStringValue)) {
                queueSizeCountLabel.setText(newStringValue);
            }
        }
    }

    @Override
    public AbstractController getController()
    {
        return controller;
    }

    private class DataListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            Integer depth = (Integer) Brain
                    .getInstance()
                    .execute(Handler
                            .makeXMLRequest(BrainXMLData
                                    .newBuild()
                                    .setJob(BrainXMLData.JOB_REQUEST)
                                    .setClazz(MetadataConsole.Data.class)
                                    .setFunction(MetadataConsole.Data.DEPTH.getMethodName()))).get();

            controller.changeQueueDeepText(String.valueOf(depth));
        }
    }

    private class AIMEBotConsoleViewPanelEvt implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() == startButton) {
                model.start();
            }
            else if (e.getSource() == stopButton) {
                model.stop();
            }
            else if (e.getSource() == showAllButton) {
                model.showAll();
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        actionPanel = new javax.swing.JPanel();
        startButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        queuePanel = new javax.swing.JPanel();
        queueSizeLabel = new javax.swing.JLabel();
        queueSizeCountLabel = new javax.swing.JLabel();
        showAllButton = new javax.swing.JButton();
        botConsolePanel = new javax.swing.JPanel();
        botConsoleScrollPane = new javax.swing.JScrollPane();
        botConsoleTable = new javax.swing.JTable();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        actionPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        actionPanel.setName("actionPanel"); // NOI18N
        actionPanel.setLayout(new javax.swing.BoxLayout(actionPanel, javax.swing.BoxLayout.LINE_AXIS));

        startButton.setText("Start");
        startButton.setName("startButton"); // NOI18N
        startButton.addActionListener(new AIMEBotConsoleViewPanelEvt());
        actionPanel.add(startButton);

        stopButton.setText("Stop");
        stopButton.setName("stopButton"); // NOI18N
        stopButton.addActionListener(new AIMEBotConsoleViewPanelEvt());
        actionPanel.add(stopButton);

        queuePanel.setName("queuePanel"); // NOI18N
        queuePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        queueSizeLabel.setText("Queue Size:");
        queueSizeLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        queueSizeLabel.setName("queueSizeLabel"); // NOI18N
        queuePanel.add(queueSizeLabel);

        queueSizeCountLabel.setText("0");
        queueSizeCountLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        queueSizeCountLabel.setName("queueSizeCountLabel"); // NOI18N
        queuePanel.add(queueSizeCountLabel);

        showAllButton.setText("Show All");
        showAllButton.setName("showAllButton"); // NOI18N
        showAllButton.addActionListener(new AIMEBotConsoleViewPanelEvt());
        queuePanel.add(showAllButton);

        actionPanel.add(queuePanel);

        add(actionPanel, java.awt.BorderLayout.NORTH);

        botConsolePanel.setName("botConsolePanel"); // NOI18N
        botConsolePanel.setLayout(new javax.swing.BoxLayout(botConsolePanel, javax.swing.BoxLayout.LINE_AXIS));

        botConsoleScrollPane.setName("botConsoleScrollPane"); // NOI18N

        botConsoleTable.setName("botConsoleTable"); // NOI18N
        botConsoleScrollPane.setViewportView(botConsoleTable);

        botConsolePanel.add(botConsoleScrollPane);

        add(botConsolePanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionPanel;
    private javax.swing.JPanel botConsolePanel;
    private javax.swing.JScrollPane botConsoleScrollPane;
    private javax.swing.JTable botConsoleTable;
    private javax.swing.JPanel queuePanel;
    private javax.swing.JLabel queueSizeCountLabel;
    private javax.swing.JLabel queueSizeLabel;
    private javax.swing.JButton showAllButton;
    private javax.swing.JButton startButton;
    private javax.swing.JButton stopButton;
    // End of variables declaration//GEN-END:variables
}
