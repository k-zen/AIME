package io.aime.mvc.view;

import net.apkc.emma.mvc.AbstractFrame;

final class CerebellumWindow extends AbstractFrame
{

    public static final int CEREBELLUMVIEWPANEL_ID = 0;
    private static final CerebellumWindow _INSTANCE = new CerebellumWindow();
    private boolean ready = false;

    final static CerebellumWindow getInstance()
    {
        return _INSTANCE;
    }

    private CerebellumWindow()
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
        add(CerebellumViewPanel.newBuild());
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
        setTitle("Cerebellum Stats Window");
        setMinimumSize(new java.awt.Dimension(320, 540));
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(320, 540));
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
