package io.aime.mvc.view;

import io.aime.mvc.model.SeedsTableModel;
import io.aime.mvc.view.tools.TableColumnAdjuster;
import io.aime.util.HtmlMessageBuilder;
import io.aime.util.SeedTools;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import net.apkc.emma.mvc.AbstractController;
import net.apkc.emma.mvc.AbstractViewPanel;

final class SeedsViewPanel extends AbstractViewPanel
{

    private SeedsTableModel model;

    final static AbstractViewPanel newBuild()
    {
        return new SeedsViewPanel();
    }

    private SeedsViewPanel()
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

        seedsTable.setModel(model = new SeedsTableModel());
        seedsTable.setRowHeight(20);
        seedsTable.setRowSelectionAllowed(false);
        seedsTable.setShowGrid(false);
        seedsTable.setGridColor(Color.DARK_GRAY);
        seedsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        TableColumnAdjuster tca = new TableColumnAdjuster(seedsTable);
        tca.setColumnHeaderIncluded(true);
        tca.adjustColumns();

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

    private class SeedsViewPanelEvt implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() == importSeedsButton)
            {
                imp(model, SeedsViewPanel.this);
            }
            else if (e.getSource() == addSeedButton)
            {
                add(model, SeedsViewPanel.this);
            }
            else if (e.getSource() == removeSeedButton)
            {
                remove(model, SeedsViewPanel.this, seedsTable);
            }
        }

        void imp(SeedsTableModel model, JPanel ref)
        {
            final JTextArea textArea = new JTextArea();
            // Enable use of custom set fonts
            textArea.setEditable(true);
            textArea.setText("");
            textArea.setLineWrap(false);
            textArea.setWrapStyleWord(false);
            textArea.setCaretPosition(0);

            final JFrame frame = new JFrame("Import Seeds");
            frame.getContentPane().setLayout(new BorderLayout());

            JLabel textLabel = new JLabel();
            textLabel.setHorizontalAlignment(SwingConstants.LEADING);
            textLabel.setText(HtmlMessageBuilder.buildHTMLMsg(
                    "<table>"
                    + "<tr>"
                    + "<td>"
                    + "<span class=\"subtitle\">Format:</span> URL, Init. Score, Fetch Interval"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>"
                    + "<ul>"
                    + "<li><span class=\"subtitle\">1.</span> Use one line per URL.</li>"
                    + "<li><span class=\"subtitle\">2.</span> Separate the different values with comas.</li>"
                    + "</ul>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"));

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
            textPanel.add(textLabel);

            JButton importButton = new JButton("Import");
            importButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    if (!textArea.getText().isEmpty())
                    {
                        SeedTools.importSeed(textArea.getText());
                        frame.dispose();
                    }
                }
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(importButton);

            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BorderLayout());
            topPanel.add(textPanel, BorderLayout.WEST);
            topPanel.add(buttonPanel, BorderLayout.EAST);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

            // Build the frame
            frame.add(topPanel, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(ref);
            frame.setVisible(true);
        }

        void add(SeedsTableModel model, JPanel ref)
        {
            String msg = HtmlMessageBuilder.buildHTMLMsg(
                    "Please enter the URL in the box bellow.<br/>"
                    + "<u>Example:</u><br/>"
                    + "http://www.example.com (For HTTP Protocol.)<br/>"
                    + "file:///Users/user (For File Protocol)<br/>"
                    + "smb://127.0.0.1/user/folder (For File Server Protocol)");
            String userInput = JOptionPane.showInputDialog(ref, msg, "Enter URL", JOptionPane.PLAIN_MESSAGE);

            if (userInput == null || userInput.isEmpty())
            {
                return;
            }

            // Add seed to dbase.
            SeedTools.addSeed(userInput);
        }

        void remove(SeedsTableModel model, JPanel ref, JTable tab)
        {
            // Traverse all rows, to see if a seed has been marked for deletion.
            int selected = 0;
            int counter = 0;
            int[] rows;
            for (int k = 0; k < tab.getRowCount(); k++)
            {
                if ((Boolean) tab.getValueAt(k, 1))
                {
                    selected++;
                }
            }
            rows = new int[selected];
            for (int k = 0; k < tab.getRowCount(); k++)
            {
                if ((Boolean) tab.getValueAt(k, 1))
                {
                    rows[counter] = k;
                    counter++;
                }
            }

            // If there are seeds marked for removal, then ask the user if he wants to continue.
            if (selected > 0)
            {
                int response = JOptionPane.showConfirmDialog(
                        ref,
                        "Are you sure you want to do this...?",
                        "Delete Seeds Confirmation",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION)
                {
                    model.deleteRows(rows); // Delete rows in bulk.
                }
            }
            else
            {
                JOptionPane.showMessageDialog(
                        ref,
                        "First select the seeds you want to remove.",
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        containerPanel = new javax.swing.JPanel();
        tablePanel = new javax.swing.JPanel();
        seedsScrollPane = new javax.swing.JScrollPane();
        seedsTable = new javax.swing.JTable();
        buttonsPanel = new javax.swing.JPanel();
        importSeedsButton = new javax.swing.JButton();
        addSeedButton = new javax.swing.JButton();
        removeSeedButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        containerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Seeds"));
        containerPanel.setName("containerPanel"); // NOI18N
        containerPanel.setLayout(new java.awt.BorderLayout());

        tablePanel.setName("tablePanel"); // NOI18N
        tablePanel.setLayout(new javax.swing.BoxLayout(tablePanel, javax.swing.BoxLayout.LINE_AXIS));

        seedsScrollPane.setName("seedsScrollPane"); // NOI18N

        seedsTable.setName("seedsTable"); // NOI18N
        seedsScrollPane.setViewportView(seedsTable);

        tablePanel.add(seedsScrollPane);

        containerPanel.add(tablePanel, java.awt.BorderLayout.CENTER);

        buttonsPanel.setName("buttonsPanel"); // NOI18N

        importSeedsButton.setText("Import");
        importSeedsButton.setName("importSeedsButton"); // NOI18N
        importSeedsButton.addActionListener(new SeedsViewPanelEvt());
        buttonsPanel.add(importSeedsButton);

        addSeedButton.setText("Add");
        addSeedButton.setName("addSeedButton"); // NOI18N
        addSeedButton.addActionListener(new SeedsViewPanelEvt());
        buttonsPanel.add(addSeedButton);

        removeSeedButton.setText("Remove");
        removeSeedButton.setName("removeSeedButton"); // NOI18N
        removeSeedButton.addActionListener(new SeedsViewPanelEvt());
        buttonsPanel.add(removeSeedButton);

        helpButton.setText("Help");
        helpButton.setName("helpButton"); // NOI18N
        helpButton.addActionListener(new SeedsViewPanelEvt());
        buttonsPanel.add(helpButton);

        containerPanel.add(buttonsPanel, java.awt.BorderLayout.PAGE_END);

        add(containerPanel);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSeedButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JButton helpButton;
    private javax.swing.JButton importSeedsButton;
    private javax.swing.JButton removeSeedButton;
    private javax.swing.JScrollPane seedsScrollPane;
    private javax.swing.JTable seedsTable;
    private javax.swing.JPanel tablePanel;
    // End of variables declaration//GEN-END:variables
}
