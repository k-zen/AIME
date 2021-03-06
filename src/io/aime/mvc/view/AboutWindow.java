package io.aime.mvc.view;

import net.apkc.emma.mvc.AbstractFrame;

final class AboutWindow extends AbstractFrame
{

    public static final int ABOUTVIEWPANEL_ID = 0;
    private static final AboutWindow _INSTANCE = new AboutWindow();
    private boolean ready = false;

    final static AboutWindow getInstance()
    {
        return _INSTANCE;
    }

    private AboutWindow()
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
        add(AboutViewPanel.newBuild());
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
            configure();
            setVisible(true);
        }

        return this;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About A.I.M.E.");
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(400, 500));
        setResizable(false);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
