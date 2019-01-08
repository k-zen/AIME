package io.aime.mvc.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import net.apkc.emma.mvc.AbstractFrame;

final class CrawlJobWindow extends AbstractFrame
{

    public static final int CRAWLJOBVIEWPANEL_ID = 0;
    private static final CrawlJobWindow _INSTANCE = new CrawlJobWindow();
    private boolean ready = false;

    final static CrawlJobWindow getInstance()
    {
        return _INSTANCE;
    }

    private CrawlJobWindow()
    {
        createGUI();
    }

    @Override
    protected AbstractFrame createGUI()
    {
        initComponents();
        return this;
    }

    @Override
    public AbstractFrame configure()
    {
        // ADD VIEWS TO THIS FRAME
        add(CrawlJobViewPanel.newBuild(), BorderLayout.CENTER);
        ready = true;
        return this;
    }

    @Override
    public AbstractFrame makeVisible()
    {
        setLocationRelativeTo(MainWindow.getInstance());
        if (ready)
        {
            if (!isVisible())
            {
                setVisible(true);
            }
            else
            {
                toFront();
            }
        }
        else
        {
            EventQueue.invokeLater(() ->
            {
                configure();
                setVisible(true);
            });
        }

        return this;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Crawl Job");
        setMinimumSize(new java.awt.Dimension(400, 360));
        setName("Form"); // NOI18N
        setResizable(false);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
