package io.aime.mvc.view;

import io.aime.mvc.controller.DefaultController;
import io.aime.mvc.model.DefaultModel;
import io.aime.util.HtmlMessageBuilder;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import net.apkc.emma.mvc.AbstractController;
import net.apkc.emma.mvc.AbstractViewPanel;

final class AboutViewPanel extends AbstractViewPanel
{

    private DefaultModel model;
    private DefaultController controller;

    final static AbstractViewPanel newBuild()
    {
        return new AboutViewPanel();
    }

    private AboutViewPanel()
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
        controller = new DefaultController();
        // CONFIGURE MODELS
        controller.addModel(model = new DefaultModel());
        // CONFIGURE VIEWS
        controller.addView(this);

        StringBuilder text = new StringBuilder();
        text.append("<html>");
        text.append("<head>");
        text.append(HtmlMessageBuilder.mainCSS());
        text.append("</head>");
        text.append("<body>");
        text.append("<div id=\"container\">");
        text.append("<table>");
        text.append("<tr>");
        text.append("<td>");
        text.append("<p>");
        text.append("<b>A.I.M.E.</b> stands for <u>Automated Indexer & (Data) Mining Engine</u>. This means that <b>A.I.M.E.</b> can crawl the Web and ");
        text.append("index information she founds. <b>A.I.M.E.</b> was developed as a side/hobby project by computer programmer <b>Andreas Koenzen</b> ");
        text.append("aproximately 3 years ago. She is now in her 0.2 version. <b>A.I.M.E.</b> was build using the Java programming language and it's ");
        text.append("based on the Open Source search engine <a href=\"http://nutch.apache.org\">Nutch</a>, which has <a href=\"http://www.apache.org/licenses/LICENSE-2.0\">Apache Licence</a>. ");
        text.append("The base code for this project was borrowed from this great software. <b>A.I.M.E.</b> is a search engine build entirely over the <a href=\"http://hadoop.apache.org\">Hadoop</a> ");
        text.append("platform, so she has support for <a href=\"http://research.google.com/archive/mapreduce.html\">MapReduce</a> processing, supporting ");
        text.append("distributed processing of gathered data/documents. This is very important, because this puts an upperbound on <b>A.I.M.E.</b> on ");
        text.append("hardware instead of software. She can crawl and index the Web very fast and very easily using her ");
        text.append("<a href=\"http://en.wikipedia.org/wiki/Graphical_user_interface\">Graphical User Interface (GUI)</a>. <b>A.I.M.E.</b> its not ");
        text.append("only fast and efficient, but she is also polite when she crawls your Web page, you can instruct her on how would you like your ");
        text.append("page to be crawled or if you don\'t want it to be crawled at all. For more info, keep reading ...");
        text.append("</p>");
        text.append("</td>");
        text.append("</tr>");
        text.append("<tr>");
        text.append("<th>Technical description of <b>A.I.M.E.</b>:</th>");
        text.append("</tr>");
        text.append("<tr>");
        text.append("<td>");
        text.append("<h1>Support for distributed processing:</h1>");
        text.append("<p>");
        text.append("Through the Hadoop framework <b>A.I.M.E.</b> has support for MapReduce and distributed processing, ");
        text.append("this is necessary given the current size of the Web, and also because it's a LOT faster!!!");
        text.append("</p>");
        text.append("<h1>Support for distributed indexing:</h1>");
        text.append("<p>");
        text.append("<b>A.I.M.E.</b> possesses her own indexing manager named the AIME's Kernel, which can support many ");
        text.append("instances, each instance containing a small fraction of the total index. This makes <b>A.I.M.E.</b> fault-tolerant and fast for searching.");
        text.append("</p>");
        text.append("<h1>Support for incremental indexing:</h1>");
        text.append("<p>");
        text.append("<b>A.I.M.E.</b> uses an index in DFS (Distributed File System) for storing the buffer index, which will ");
        text.append("provide the necessary data after each iteration of the crawling process to build the final index which will ");
        text.append("be stored in the Kernel. This final index gets created only once during the first iteration and from there ");
        text.append("documents/data gets added or updated. This brings support for incremental indexing capabilities.");
        text.append("</p>");
        text.append("<h1>Support for near real-time search:</h1>");
        text.append("<p>");
        text.append("<b>A.I.M.E.</b>'s Kernel can be queried anytime, she can search the kernel while it is being updated or a new document is being added to it.");
        text.append("</p>");
        text.append("<h1>Centralized Control Dashboard:</h1>");
        text.append("<p>");
        text.append("<b>A.I.M.E.</b> has a centralized dashboard that runs in the JobTracker and it\'s build using Java Swing. This dashboard ");
        text.append("can provide accurate stats of the running processes. Like how many pages were fetched and how many where blocked by the ");
        text.append("robots.txt directive, etc. For more information check out the screenshots below.");
        text.append("</p>");
        text.append("</td>");
        text.append("</tr>");
        text.append("</table>");
        text.append("</div>");
        text.append("</body>");
        text.append("</html>");
        controller.changeAboutText(text.toString());

        return this;
    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals(DefaultController.ABOUT_TEXT_PROPERTY))
        {
            String newStringValue = evt.getNewValue().toString();
            if (!textEditorPane.getText().equals(newStringValue))
            {
                textEditorPane.setText(newStringValue);
                textEditorPane.setCaretPosition(0);
            }
        }
    }

    @Override
    public AbstractController getController()
    {
        return controller;
    }

    private class AboutViewPanelEvt implements HyperlinkListener
    {

        @Override
        public void hyperlinkUpdate(final HyperlinkEvent e)
        {
            if (e.getEventType() == HyperlinkEvent.EventType.ENTERED)
            {
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        SwingUtilities.getWindowAncestor(textEditorPane).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Show hand cursor
                        textEditorPane.setToolTipText(e.getURL().toExternalForm()); // Show URL as the tooltip
                    }
                });
            }
            else if (e.getEventType() == HyperlinkEvent.EventType.EXITED)
            {
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        SwingUtilities.getWindowAncestor(textEditorPane).setCursor(Cursor.getDefaultCursor()); // Show default cursor
                        textEditorPane.setToolTipText(null); // Reset tooltip
                    }
                });
            }
            else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
            {
                if (Desktop.isDesktopSupported())
                {
                    try
                    {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    }
                    catch (URISyntaxException | IOException ex)
                    {
                        // LOG.error("Generic error on link listener.", ex);
                    }
                }
                else
                {
                    // LOG.error("Hyperlinking not supported here.");
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        containerPanel = new javax.swing.JPanel();
        topPanel = new javax.swing.JPanel();
        logoLabel = new javax.swing.JLabel();
        centerPanel = new javax.swing.JPanel();
        textScrollPane = new javax.swing.JScrollPane();
        textEditorPane = new javax.swing.JEditorPane();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        containerPanel.setName("containerPanel"); // NOI18N
        containerPanel.setLayout(new java.awt.BorderLayout());

        topPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.setName("topPanel"); // NOI18N

        logoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/img/Logo.png"))); // NOI18N
        logoLabel.setName("logoLabel"); // NOI18N
        topPanel.add(logoLabel);

        containerPanel.add(topPanel, java.awt.BorderLayout.NORTH);

        centerPanel.setName("centerPanel"); // NOI18N
        centerPanel.setLayout(new javax.swing.BoxLayout(centerPanel, javax.swing.BoxLayout.LINE_AXIS));

        textScrollPane.setBorder(null);
        textScrollPane.setName("textScrollPane"); // NOI18N

        textEditorPane.setEditable(false);
        textEditorPane.setContentType("text/html"); // NOI18N
        textEditorPane.setName("textEditorPane"); // NOI18N
        textEditorPane.addHyperlinkListener(new AboutViewPanelEvt());
        textScrollPane.setViewportView(textEditorPane);

        centerPanel.add(textScrollPane);

        containerPanel.add(centerPanel, java.awt.BorderLayout.CENTER);

        add(containerPanel);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JLabel logoLabel;
    private javax.swing.JEditorPane textEditorPane;
    private javax.swing.JScrollPane textScrollPane;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
