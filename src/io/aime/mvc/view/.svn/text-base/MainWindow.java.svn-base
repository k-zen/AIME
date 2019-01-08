package io.aime.mvc.view;

import io.aime.tasks.init.BrainStartTask;
import io.aime.util.AIMEConstants;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import net.apkc.emma.mvc.AbstractFrame;
import net.apkc.emma.tasks.TasksHandler;
import net.apkc.emma.utils.Fonts;
import org.apache.log4j.Logger;

final class MainWindow extends AbstractFrame
{

    public static final int AIMEDASHBOARDVIEWPANEL_ID = 0;
    public static final int AIMEJOBSVIEWPANEL_ID = 1;
    public static final int AIMEEVENTSVIEWPANEL_ID = 2;
    public static final int AIMEBOTCONSOLEVIEWPANEL_ID = 3;
    private static final Logger LOG = Logger.getLogger(MainWindow.class.getName());
    private static MainWindow _INSTANCE;
    private int executionType = 0;
    private int heapSize = 0;

    final static MainWindow getInstance()
    {
        return _INSTANCE;
    }

    private MainWindow()
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
        setJMenuBar(new MainViewMenuBar());
        dashboardPanel.add(AIMEDashboardViewPanel.newBuild());
        leftContainerPanel.add(AIMEJobsViewPanel.newBuild());
        rightContainerPanel.add(AIMEEventsViewPanel.newBuild());
        consolePanel.add(AIMEBotConsoleViewPanel.newBuild());
        // Paint panel.
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                JOptionPane.showMessageDialog(
                        MainWindow.getInstance(),
                        "To exit the application go to FILE => EXIT.",
                        "HELP",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        TasksHandler.getInstance().submitInfiniteTask(BrainStartTask.newBuild().setExecutionType(executionType).setHeapSize(heapSize));

        return this;
    }

    @Override
    public AbstractFrame makeVisible()
    {
        setVisible(true);
        return this;
    }

    public static void main(final String args[])
    {
        try
        {
            UIManager.setLookAndFeel(new NimbusLookAndFeel()
            {
                @Override
                public UIDefaults getDefaults()
                {
                    UIDefaults ret = super.getDefaults();
                    ret.put("nimbusBase", new Color(45, 50, 56));
                    ret.put("control", new Color(45, 50, 56));
                    ret.put("text", new Color(216, 216, 200));
                    ret.put("nimbusLightBackground", new Color(118, 131, 148));
                    ret.put("defaultFont", Fonts.getInstance().getDefaultBold());
                    // Button Color
                    ret.put("Button.background", new Color(31, 31, 31));
                    ret.put("Button.textForeground", new Color(251, 251, 251));
                    // ScrollPane
                    ret.put("ScrollPane[Enabled].borderPainter", null);

                    return ret;
                }
            });
        }
        catch (UnsupportedLookAndFeelException e)
        {
            LOG.error("Impossible to set the \"L&F\" for the application. Error: " + e.toString(), e);
        }

        int executionType = 0;
        int heapSize = 0;
        if (args != null)
        {
            executionType = ((args[0].equalsIgnoreCase("local")) ? AIMEConstants.LOCAL_EXECUTION_TYPE.getIntegerConstant() : AIMEConstants.DISTRIBUTED_EXECUTION_TYPE.getIntegerConstant());
            try
            {
                if (executionType == AIMEConstants.LOCAL_EXECUTION_TYPE.getIntegerConstant())
                {
                    heapSize = Integer.parseInt(args[1]);
                }
            }
            catch (NumberFormatException e)
            {
                LOG.error("The value of the configured JVM Heap size could not be parsed. Error: " + e.toString(), e);
            }
        }

        // <<< BUILD THE GUI AND SHOW SOMETHING >>>
        _INSTANCE = new MainWindow();
        _INSTANCE.setParams(executionType, heapSize).configure().makeVisible();
        // <<< BUILD THE GUI AND SHOW SOMETHING >>>
    }

    /**
     * Mutator method for setting the parameters passed on by the launching
     * script.
     *
     * @param executionType The type of the execution. 1 : Local, 2 : Distributed
     * @param heapSize      JVM's HEAP size.
     *
     * @return This instance.
     */
    private MainWindow setParams(int executionType, int heapSize)
    {
        this.executionType = executionType;
        this.heapSize = heapSize;

        return this;
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        mainSplitPane = new javax.swing.JSplitPane();
        topPanel = new javax.swing.JPanel();
        dashboardPanel = new javax.swing.JPanel();
        lowerPanel = new javax.swing.JPanel();
        leftContainerPanel = new javax.swing.JPanel();
        rightContainerPanel = new javax.swing.JPanel();
        bottomPanel = new javax.swing.JPanel();
        consolePanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("A.I.M.E. v0.2");
        setMinimumSize(new java.awt.Dimension(300, 660));
        setName("Form"); // NOI18N
        setResizable(false);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.LINE_AXIS));

        mainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setResizeWeight(.5d);
        mainSplitPane.setName("mainSplitPane"); // NOI18N

        topPanel.setName("topPanel"); // NOI18N
        topPanel.setLayout(new java.awt.BorderLayout());

        dashboardPanel.setName("dashboardPanel"); // NOI18N
        dashboardPanel.setLayout(new javax.swing.BoxLayout(dashboardPanel, javax.swing.BoxLayout.LINE_AXIS));
        topPanel.add(dashboardPanel, java.awt.BorderLayout.NORTH);

        lowerPanel.setName("lowerPanel"); // NOI18N
        lowerPanel.setLayout(new javax.swing.BoxLayout(lowerPanel, javax.swing.BoxLayout.Y_AXIS));

        leftContainerPanel.setName("leftContainerPanel"); // NOI18N
        leftContainerPanel.setLayout(new javax.swing.BoxLayout(leftContainerPanel, javax.swing.BoxLayout.LINE_AXIS));
        lowerPanel.add(leftContainerPanel);

        rightContainerPanel.setName("rightContainerPanel"); // NOI18N
        rightContainerPanel.setLayout(new javax.swing.BoxLayout(rightContainerPanel, javax.swing.BoxLayout.LINE_AXIS));
        lowerPanel.add(rightContainerPanel);

        topPanel.add(lowerPanel, java.awt.BorderLayout.CENTER);

        mainSplitPane.setLeftComponent(topPanel);

        bottomPanel.setName("bottomPanel"); // NOI18N
        bottomPanel.setLayout(new javax.swing.BoxLayout(bottomPanel, javax.swing.BoxLayout.LINE_AXIS));

        consolePanel.setName("consolePanel"); // NOI18N
        consolePanel.setLayout(new javax.swing.BoxLayout(consolePanel, javax.swing.BoxLayout.LINE_AXIS));
        bottomPanel.add(consolePanel);

        mainSplitPane.setRightComponent(bottomPanel);

        getContentPane().add(mainSplitPane);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JPanel consolePanel;
    private javax.swing.JPanel dashboardPanel;
    private javax.swing.JPanel leftContainerPanel;
    private javax.swing.JPanel lowerPanel;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JPanel rightContainerPanel;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
