package io.aime.mvc.view;

import net.apkc.emma.mvc.AbstractFrame;

/**
 * Frame for the segment DBase.
 *
 * @author K-Zen
 * @see Singleton Pattern
 */
final class SegmentDBaseWindow extends AbstractFrame
{

    public static final int SEGMENTDBASEVIEWPANEL_ID = 0;
    private static final SegmentDBaseWindow _INSTANCE = new SegmentDBaseWindow();
    private boolean ready = false;

    final static SegmentDBaseWindow getInstance()
    {
        return _INSTANCE;
    }

    private SegmentDBaseWindow()
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
        add(SegmentDBaseViewPanel.newBuild());
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
        setTitle("Read SegmentDBase");
        setMinimumSize(new java.awt.Dimension(700, 300));
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(700, 300));
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
