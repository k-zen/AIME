package io.aime.mvc.view;

import io.aime.mvc.controller.CrawlJobController;
import java.beans.PropertyChangeEvent;
import net.apkc.emma.mvc.AbstractController;
import net.apkc.emma.mvc.AbstractViewPanel;
import net.apkc.emma.utils.Fonts;

final class AIMEDashboardViewPanel extends AbstractViewPanel
{

    final static AbstractViewPanel newBuild()
    {
        return new AIMEDashboardViewPanel();
    }

    private AIMEDashboardViewPanel()
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
        // CONTROLLERS
        // CONFIGURE MODELS
        // CONFIGURE VIEWS

        return this;
    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent evt)
    {
        switch (evt.getPropertyName())
        {
            case CrawlJobController.RUNNING_FUNCTION_TEXT_PROPERTY:
            {
                String newStringValue = evt.getNewValue().toString();
                if (!runningFunctionShowLabel.getText().equals(newStringValue))
                {
                    runningFunctionShowLabel.setText(newStringValue);
                }
                break;
            }
            case CrawlJobController.CURRENT_DEPTH_TEXT_PROPERTY:
            {
                String newStringValue = evt.getNewValue().toString();
                if (!currentDepthShowLabel.getText().equals(newStringValue))
                {
                    currentDepthShowLabel.setText(newStringValue);
                }
                break;
            }
            case CrawlJobController.JOB_TIMER_TEXT_PROPERTY:
            {
                String newStringValue = evt.getNewValue().toString();
                if (!timerShowLabel.getText().equals(newStringValue))
                {
                    timerShowLabel.setText(newStringValue);
                }
                break;
            }
            case CrawlJobController.PROGRESS_PROPERTY:
                Integer newIntegerValue = Integer.parseInt(evt.getNewValue().toString());
                if ((crawlJobProgressBar.getPercentComplete() * 100) != newIntegerValue)
                {
                    crawlJobProgressBar.setValue(newIntegerValue);
                }
                break;
        }
    }

    @Override
    public AbstractController getController()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jobInfoPanel = new javax.swing.JPanel();
        runningFunctionLabel = new javax.swing.JLabel();
        runningFunctionShowLabel = new javax.swing.JLabel();
        currentDepthLabel = new javax.swing.JLabel();
        currentDepthShowLabel = new javax.swing.JLabel();
        timerLabel = new javax.swing.JLabel();
        timerShowLabel = new javax.swing.JLabel();
        progressLabel = new javax.swing.JLabel();
        crawlJobProgressBar = new javax.swing.JProgressBar();

        setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6), javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Current Job:", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, Fonts.getInstance().getDefaultBold(14))));
        setName("Form"); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jobInfoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jobInfoPanel.setName("jobInfoPanel"); // NOI18N
        jobInfoPanel.setLayout(new java.awt.GridLayout(4, 2));

        runningFunctionLabel.setText("Function:");
        runningFunctionLabel.setName("runningFunctionLabel"); // NOI18N
        jobInfoPanel.add(runningFunctionLabel);

        runningFunctionShowLabel.setText("none");
        runningFunctionShowLabel.setName("runningFunctionShowLabel"); // NOI18N
        jobInfoPanel.add(runningFunctionShowLabel);

        currentDepthLabel.setText("Current Depth:");
        currentDepthLabel.setName("currentDepthLabel"); // NOI18N
        jobInfoPanel.add(currentDepthLabel);

        currentDepthShowLabel.setText("0");
        currentDepthShowLabel.setName("currentDepthShowLabel"); // NOI18N
        jobInfoPanel.add(currentDepthShowLabel);

        timerLabel.setText("Timer:");
        timerLabel.setName("timerLabel"); // NOI18N
        jobInfoPanel.add(timerLabel);

        timerShowLabel.setText("000 : 00 : 00 : 00");
        timerShowLabel.setName("timerShowLabel"); // NOI18N
        jobInfoPanel.add(timerShowLabel);

        progressLabel.setText("Job Progress:");
        progressLabel.setName("progressLabel"); // NOI18N
        jobInfoPanel.add(progressLabel);

        crawlJobProgressBar.setName("crawlJobProgressBar"); // NOI18N
        crawlJobProgressBar.setOpaque(true);
        crawlJobProgressBar.setStringPainted(true);
        jobInfoPanel.add(crawlJobProgressBar);

        add(jobInfoPanel);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar crawlJobProgressBar;
    private javax.swing.JLabel currentDepthLabel;
    private javax.swing.JLabel currentDepthShowLabel;
    private javax.swing.JPanel jobInfoPanel;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JLabel runningFunctionLabel;
    private javax.swing.JLabel runningFunctionShowLabel;
    private javax.swing.JLabel timerLabel;
    private javax.swing.JLabel timerShowLabel;
    // End of variables declaration//GEN-END:variables
}
