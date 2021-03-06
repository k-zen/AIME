package io.aime.mvc.view;

import io.aime.bot.ConsoleMessage;
import io.aime.brain.Brain;
import io.aime.brain.data.BrainXMLData;
import io.aime.brain.data.MetadataConsole;
import io.aime.brain.xml.Handler;
import io.aime.crawl.CrawlDBReader;
import io.aime.util.AIMEConfiguration;
import io.aime.util.AIMEConstants;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import net.apkc.emma.mvc.AbstractController;
import net.apkc.emma.mvc.AbstractViewPanel;
import net.apkc.emma.tasks.Task;
import net.apkc.emma.tasks.TasksHandler;
import org.apache.log4j.Logger;

final class MainDBaseViewPanel extends AbstractViewPanel
{

    private static final Logger LOG = Logger.getLogger(MainDBaseViewPanel.class.getName());

    final static AbstractViewPanel newBuild()
    {
        return new MainDBaseViewPanel();
    }

    private MainDBaseViewPanel()
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

    private class MainDBaseViewPanelEvt implements ActionListener, HyperlinkListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() == launchButton) {
                launch(MainDBaseViewPanel.this);
            }
        }

        /**
         * Launches a given task.
         *
         * @param ref A window reference
         */
        void launch(JPanel ref)
        {
            searchProgressBar.setIndeterminate(true);
            launchButton.setEnabled(false); // Disable button
            try {
                TasksHandler.getInstance().submitFiniteTask(new RunMainDBaseJobTask().setTask(functionsComboBox.getSelectedIndex()));
            }
            catch (Exception e) {
                LOG.error("Problem launching task.", e);

                // Notify the console.
                Brain
                        .getClient(new AIMEConfiguration().create())
                        .execute(Handler
                                .makeXMLRequest(
                                        BrainXMLData
                                        .newBuild()
                                        .setJob(BrainXMLData.JOB_MERGE)
                                        .setClazz(MetadataConsole.Data.class)
                                        .setFunction(MetadataConsole.Data.MESSAGE.getMethodName())
                                        .setParam(BrainXMLData.Parameter
                                                .newBuild()
                                                .setType(ConsoleMessage.class)
                                                .setData(ConsoleMessage.newBuild().setSeverity(ConsoleMessage.ERROR).setMessage("Error processing task.")))));
            }
        }

        @Override
        public void hyperlinkUpdate(final HyperlinkEvent e)
        {
            if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                EventQueue.invokeLater(() -> {
                    SwingUtilities.getWindowAncestor(resultsTextPane).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Show hand cursor
                    resultsTextPane.setToolTipText(e.getURL().toExternalForm()); // Show URL as the tooltip
                });
            }
            else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                EventQueue.invokeLater(() -> {
                    SwingUtilities.getWindowAncestor(resultsTextPane).setCursor(Cursor.getDefaultCursor()); // Show default cursor
                    resultsTextPane.setToolTipText(null); // Reset tooltip
                });
            }
            else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    }
                    catch (URISyntaxException | IOException ex) {
                        LOG.error("Generic error on link listener.", ex);
                    }
                }
                else {
                    LOG.error("Hyperlinking not supported here.");
                }
            }
        }
    }

    private class RunMainDBaseJobTask extends Task
    {

        private int task;

        public RunMainDBaseJobTask setTask(int task)
        {
            this.task = task;
            return this;
        }

        @Override
        protected Object doInBackground() throws Exception
        {
            switch (task) {
                case 0:
                    try {
                        resultsTextPane.setText(CrawlDBReader.getInstance().processStatJob(
                                AIMEConstants.DEFAULT_JOB_NAME.getStringConstant() + "/" + AIMEConstants.AIME_CRAWLDB_DIR_NAME.getStringConstant(),
                                new AIMEConfiguration().create(),
                                false).toString());
                        searchProgressBar.setIndeterminate(false);
                        launchButton.setEnabled(true); // Revert to old icon and enable button.

                        return true;
                    }
                    catch (IOException ex) {
                        LOG.fatal("Impossible to launch job.", ex);
                    }
                    break;
                case 1:
                    try {
                        resultsTextPane.setText(CrawlDBReader.getInstance().processStatJob(
                                AIMEConstants.DEFAULT_JOB_NAME.getStringConstant() + "/" + AIMEConstants.AIME_CRAWLDB_DIR_NAME.getStringConstant(),
                                new AIMEConfiguration().create(),
                                true).toString());
                        searchProgressBar.setIndeterminate(false);
                        launchButton.setEnabled(true); // Revert to old icon and enable button.

                        return true;
                    }
                    catch (IOException ex) {
                        LOG.fatal("Impossible to launch job.", ex);
                    }
                    break;
                case 2:
                    try {
                        resultsTextPane.setText(CrawlDBReader.getInstance().processTopNJob(
                                AIMEConstants.DEFAULT_JOB_NAME.getStringConstant() + "/" + AIMEConstants.AIME_CRAWLDB_DIR_NAME.getStringConstant(),
                                1000,
                                0.0f,
                                new AIMEConfiguration().create()).toString());
                        searchProgressBar.setIndeterminate(false);
                        launchButton.setEnabled(true); // Revert to old icon and enable button.

                        return true;
                    }
                    catch (IOException ex) {
                        LOG.fatal("Impossible to launch job.", ex);
                    }
                    break;
                case 3:
                    String msg2
                           = "<html>"
                            + "<body>"
                            + "<p>"
                            + "<b>Please enter the URL in the box bellow:</b>"
                            + "<br/>"
                            + "<b><u>Example:</u></b> http://www.example.com"
                            + "</p>"
                            + "</body>"
                            + "</html>";
                    String userInput = JOptionPane.showInputDialog(MainDBaseViewPanel.this, msg2, "Enter URL", JOptionPane.PLAIN_MESSAGE);
                    if (userInput == null || userInput.isEmpty()) {
                        break;
                    }

                    try {
                        resultsTextPane.setText(CrawlDBReader.getInstance().readUrl(
                                AIMEConstants.DEFAULT_JOB_NAME.getStringConstant() + "/" + AIMEConstants.AIME_CRAWLDB_DIR_NAME.getStringConstant(),
                                userInput,
                                new AIMEConfiguration().create()).toString());
                        searchProgressBar.setIndeterminate(false);
                        launchButton.setEnabled(true); // Revert to old icon and enable button.

                        return true;
                    }
                    catch (IOException ex) {
                        LOG.fatal("Impossible to launch job.", ex);
                    }
                    break;
            }

            return false;
        }

        @Override
        public void reportProgress(int progress)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        containerPanel = new javax.swing.JPanel();
        toolsPanel = new javax.swing.JPanel();
        optionsPanel = new javax.swing.JPanel();
        functionsComboBox = new javax.swing.JComboBox();
        launchButton = new javax.swing.JButton();
        progressPanel = new javax.swing.JPanel();
        searchProgressBar = new javax.swing.JProgressBar();
        resultsPanel = new javax.swing.JPanel();
        resultsScrollPane = new javax.swing.JScrollPane();
        resultsTextPane = new javax.swing.JTextPane();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        containerPanel.setName("containerPanel"); // NOI18N
        containerPanel.setLayout(new java.awt.BorderLayout());

        toolsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Tools"));
        toolsPanel.setName("toolsPanel"); // NOI18N
        toolsPanel.setLayout(new java.awt.GridLayout(2, 1));

        optionsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        optionsPanel.setName("optionsPanel"); // NOI18N
        optionsPanel.setLayout(new java.awt.BorderLayout());

        functionsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Show Stats", "Show Stats Sorted", "Show Top Ranked URLs", "Get URL Information" }));
        functionsComboBox.setName("functionsComboBox"); // NOI18N
        optionsPanel.add(functionsComboBox, java.awt.BorderLayout.CENTER);

        launchButton.setMnemonic('l');
        launchButton.setText("Launch Query");
        launchButton.setName("launchButton"); // NOI18N
        launchButton.addActionListener(new MainDBaseViewPanelEvt());
        optionsPanel.add(launchButton, java.awt.BorderLayout.EAST);

        toolsPanel.add(optionsPanel);

        progressPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        progressPanel.setName("progressPanel"); // NOI18N
        progressPanel.setLayout(new javax.swing.BoxLayout(progressPanel, javax.swing.BoxLayout.LINE_AXIS));

        searchProgressBar.setName("searchProgressBar"); // NOI18N
        progressPanel.add(searchProgressBar);

        toolsPanel.add(progressPanel);

        containerPanel.add(toolsPanel, java.awt.BorderLayout.NORTH);

        resultsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Viewer"));
        resultsPanel.setName("resultsPanel"); // NOI18N
        resultsPanel.setLayout(new javax.swing.BoxLayout(resultsPanel, javax.swing.BoxLayout.LINE_AXIS));

        resultsScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        resultsScrollPane.setName("resultsScrollPane"); // NOI18N

        resultsTextPane.setEditable(false);
        resultsTextPane.setContentType("text/html"); // NOI18N
        resultsTextPane.setName("resultsTextPane"); // NOI18N
        resultsTextPane.addHyperlinkListener(new MainDBaseViewPanelEvt());
        resultsScrollPane.setViewportView(resultsTextPane);

        resultsPanel.add(resultsScrollPane);

        containerPanel.add(resultsPanel, java.awt.BorderLayout.CENTER);

        add(containerPanel);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel containerPanel;
    private javax.swing.JComboBox functionsComboBox;
    private javax.swing.JButton launchButton;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JPanel progressPanel;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JScrollPane resultsScrollPane;
    private javax.swing.JTextPane resultsTextPane;
    private javax.swing.JProgressBar searchProgressBar;
    private javax.swing.JPanel toolsPanel;
    // End of variables declaration//GEN-END:variables
}
