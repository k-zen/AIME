package io.aime.mvc.view;

import io.aime.mvc.controller.FilterRuleController;
import io.aime.mvc.model.FilterRuleModel;
import io.aime.mvc.model.FilterSiteRuleTableModel;
import io.aime.mvc.model.FilterURLRuleTableModel;
import io.aime.mvc.view.tools.RequestFocusListener;
import io.aime.mvc.view.tools.TableColumnAdjuster;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import net.apkc.emma.mvc.AbstractController;
import net.apkc.emma.mvc.AbstractViewPanel;

final class FilterRuleViewPanel extends AbstractViewPanel
{

    private FilterRuleController controller;
    private FilterURLRuleTableModel model1;
    private FilterSiteRuleTableModel model2;

    final static AbstractViewPanel newBuild()
    {
        return new FilterRuleViewPanel();
    }

    private FilterRuleViewPanel()
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
        controller = new FilterRuleController();
        // CONFIGURE MODELS
        controller.addModel(new FilterRuleModel());
        // CONFIGURE VIEWS
        controller.addView(this);

        urlRulesTable.setModel(model1 = new FilterURLRuleTableModel());
        urlRulesTable.setRowHeight(20);
        urlRulesTable.setRowSelectionAllowed(false);
        urlRulesTable.setShowGrid(false);
        urlRulesTable.setGridColor(Color.DARK_GRAY);
        urlRulesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        TableColumnAdjuster tca1 = new TableColumnAdjuster(urlRulesTable);
        tca1.setColumnHeaderIncluded(true);
        tca1.adjustColumns();

        sitesRulesTable.setModel(model2 = new FilterSiteRuleTableModel());
        sitesRulesTable.setRowHeight(20);
        sitesRulesTable.setRowSelectionAllowed(false);
        sitesRulesTable.setShowGrid(false);
        sitesRulesTable.setGridColor(Color.DARK_GRAY);
        sitesRulesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        TableColumnAdjuster tca2 = new TableColumnAdjuster(sitesRulesTable);
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
        return controller;
    }

    private class FilterRuleViewPanelEvt implements ActionListener
    {

        final byte MODEL1_ID = 0x1;
        final byte MODEL2_ID = 0x2;

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() == addURLButton)
            {
                addRule(model1, model2, FilterRuleViewPanel.this, MODEL1_ID);
            }
            else if (e.getSource() == removeURLButton)
            {
                removeRule(model1, model2, FilterRuleViewPanel.this, urlRulesTable, MODEL1_ID);
            }
            else if (e.getSource() == addSiteButton)
            {
                addRule(model1, model2, FilterRuleViewPanel.this, MODEL2_ID);
            }
            else if (e.getSource() == removeSiteButton)
            {
                removeRule(model1, model2, FilterRuleViewPanel.this, sitesRulesTable, MODEL2_ID);
            }
        }

        /**
         * Adds a given rule entered by the user to a given table model.
         *
         * @param model1  URL rules table model
         * @param model2  Site rules table model
         * @param ref     A window reference
         * @param modelId Which model to use
         */
        void addRule(FilterURLRuleTableModel model1, FilterSiteRuleTableModel model2, JPanel ref, byte modelId)
        {
            JPanel panel = new JPanel();
            JLabel title = new JLabel("Please enter the Rule in the box bellow.");
            JLabel patternText = new JLabel("Pattern:");
            JTextField pattern = new JTextField();
            JLabel signText = new JLabel("Sign:");
            JComboBox sign = new JComboBox(new String[]
            {
                "Allow", "Deny"
            });
            sign.setSelectedIndex(0);
            panel.setLayout(new GridLayout(5, 1));
            panel.add(title);
            panel.add(patternText);
            panel.add(pattern);
            panel.add(signText);
            panel.add(sign);
            pattern.addAncestorListener(new RequestFocusListener());

            if (JOptionPane.showConfirmDialog(ref, panel, "Enter Rule", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
            {
                List<Object> rule = new ArrayList<>();
                rule.add(pattern.getText());
                rule.add(sign.getItemAt(sign.getSelectedIndex()).toString().equalsIgnoreCase("allow"));
                rule.add(false);

                switch (modelId)
                {
                    case MODEL1_ID:
                        model1.addNewRow(rule);
                        break;
                    case MODEL2_ID:
                        model2.addNewRow(rule);
                        break;
                }
            }
        }

        /**
         * Removes a given rule from the table model.
         *
         * @param model1  URL rules table model
         * @param model2  Site rules table model
         * @param ref     A window reference
         * @param modelId Which model to use
         */
        void removeRule(FilterURLRuleTableModel model1, FilterSiteRuleTableModel model2, JPanel ref, JTable tab, byte modelId)
        {
            // Traverse all rows, to see if a filter has been marked for deletion.
            int selected = 0;
            for (int k = 0; k < tab.getRowCount(); k++)
            {
                if ((Boolean) tab.getValueAt(k, 2))
                {
                    selected++;
                }
            }

            // If there are seeds marked for removal, then ask the user if he wants to continue.
            if (selected > 0)
            {
                // Ask the user.
                int response = JOptionPane.showConfirmDialog(
                        ref,
                        "Are you sure you want to do this...?",
                        "Delete Filter Confirmation",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION)
                {
                    // Traverser again the filters, and remove the ones that are marked.
                    for (int k = tab.getRowCount() - 1; k >= 0; k--)
                    {
                        if ((Boolean) tab.getValueAt(k, 2))
                        {
                            switch (modelId)
                            {
                                case MODEL1_ID:
                                    model1.deleteRow(k);
                                    break;
                                case MODEL2_ID:
                                    model2.deleteRow(k);
                                    break;
                            }
                        }
                    }
                }
            }
            else
            {
                JOptionPane.showMessageDialog(
                        ref,
                        "First select the filters you want to remove.",
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        rulesPanel = new javax.swing.JPanel();
        urlRulesPanel = new javax.swing.JPanel();
        urlButtonsPanel = new javax.swing.JPanel();
        addURLButton = new javax.swing.JButton();
        removeURLButton = new javax.swing.JButton();
        urlRulesScrollPane = new javax.swing.JScrollPane();
        urlRulesTable = new javax.swing.JTable();
        siteRulePanel = new javax.swing.JPanel();
        siteButtonsPanel = new javax.swing.JPanel();
        addSiteButton = new javax.swing.JButton();
        removeSiteButton = new javax.swing.JButton();
        siteRulesScrollPane = new javax.swing.JScrollPane();
        sitesRulesTable = new javax.swing.JTable();

        setName("Form"); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        rulesPanel.setName("rulesPanel"); // NOI18N
        rulesPanel.setLayout(new java.awt.GridLayout(2, 1));

        urlRulesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("URL Rules"));
        urlRulesPanel.setName("urlRulesPanel"); // NOI18N
        urlRulesPanel.setLayout(new java.awt.BorderLayout());

        urlButtonsPanel.setName("urlButtonsPanel"); // NOI18N

        addURLButton.setMnemonic('A');
        addURLButton.setText("Add");
        addURLButton.setName("addURLButton"); // NOI18N
        addURLButton.addActionListener(new FilterRuleViewPanelEvt());
        urlButtonsPanel.add(addURLButton);

        removeURLButton.setMnemonic('R');
        removeURLButton.setText("Remove");
        removeURLButton.setName("removeURLButton"); // NOI18N
        removeURLButton.addActionListener(new FilterRuleViewPanelEvt());
        urlButtonsPanel.add(removeURLButton);

        urlRulesPanel.add(urlButtonsPanel, java.awt.BorderLayout.PAGE_START);

        urlRulesScrollPane.setName("urlRulesScrollPane"); // NOI18N

        urlRulesTable.setName("urlRulesTable"); // NOI18N
        urlRulesScrollPane.setViewportView(urlRulesTable);

        urlRulesPanel.add(urlRulesScrollPane, java.awt.BorderLayout.CENTER);

        rulesPanel.add(urlRulesPanel);

        siteRulePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Site Rules"));
        siteRulePanel.setName("siteRulePanel"); // NOI18N
        siteRulePanel.setLayout(new java.awt.BorderLayout());

        siteButtonsPanel.setName("siteButtonsPanel"); // NOI18N

        addSiteButton.setMnemonic('A');
        addSiteButton.setText("Add");
        addSiteButton.setName("addSiteButton"); // NOI18N
        addSiteButton.addActionListener(new FilterRuleViewPanelEvt());
        siteButtonsPanel.add(addSiteButton);

        removeSiteButton.setMnemonic('R');
        removeSiteButton.setText("Remove");
        removeSiteButton.setName("removeSiteButton"); // NOI18N
        removeSiteButton.addActionListener(new FilterRuleViewPanelEvt());
        siteButtonsPanel.add(removeSiteButton);

        siteRulePanel.add(siteButtonsPanel, java.awt.BorderLayout.PAGE_START);

        siteRulesScrollPane.setName("siteRulesScrollPane"); // NOI18N

        sitesRulesTable.setName("sitesRulesTable"); // NOI18N
        siteRulesScrollPane.setViewportView(sitesRulesTable);

        siteRulePanel.add(siteRulesScrollPane, java.awt.BorderLayout.CENTER);

        rulesPanel.add(siteRulePanel);

        add(rulesPanel);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSiteButton;
    private javax.swing.JButton addURLButton;
    private javax.swing.JButton removeSiteButton;
    private javax.swing.JButton removeURLButton;
    private javax.swing.JPanel rulesPanel;
    private javax.swing.JPanel siteButtonsPanel;
    private javax.swing.JPanel siteRulePanel;
    private javax.swing.JScrollPane siteRulesScrollPane;
    private javax.swing.JTable sitesRulesTable;
    private javax.swing.JPanel urlButtonsPanel;
    private javax.swing.JPanel urlRulesPanel;
    private javax.swing.JScrollPane urlRulesScrollPane;
    private javax.swing.JTable urlRulesTable;
    // End of variables declaration//GEN-END:variables
}
