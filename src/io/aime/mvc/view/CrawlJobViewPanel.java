package io.aime.mvc.view;

import io.aime.brain.data.MetadataCrawlJob;
import io.aime.mvc.controller.CrawlJobController;
import io.aime.mvc.model.CrawlJobModel;
import io.aime.tasks.run.RunCrawlJobTask;
import io.aime.util.AIMEConstants;
import io.aime.util.HtmlMessageBuilder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import net.apkc.emma.mvc.AbstractController;
import net.apkc.emma.mvc.AbstractViewPanel;
import net.apkc.emma.tasks.TasksHandler;
import net.apkc.emma.utils.Fonts;
import org.apache.commons.lang.math.NumberUtils;

final class CrawlJobViewPanel extends AbstractViewPanel
{

    private static final int UPDATE_INTERVAL = 1000;
    private CrawlJobController controller;
    private CrawlJobModel model;
    private UpdateTimerLabel timer1;
    private UpdateRunningFunctionLabel timer2;
    private UpdateDepthLabel timer3;
    private UpdateProgress timer4;
    private RunCrawlJobTask job;

    final static AbstractViewPanel newBuild()
    {
        return new CrawlJobViewPanel();
    }

    private CrawlJobViewPanel()
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
        controller = new CrawlJobController();
        // CONFIGURE MODELS
        controller.addModel(model = new CrawlJobModel());
        // CONFIGURE VIEWS
        controller.addView(this);

        timer1 = new UpdateTimerLabel();
        timer2 = new UpdateRunningFunctionLabel();
        timer3 = new UpdateDepthLabel();
        timer4 = new UpdateProgress();
        job = RunCrawlJobTask.newBuild();
        timer2.start();
        timer3.start();
        timer4.start();

        return this;
    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent evt)
    {
        switch (evt.getPropertyName()) {
            case CrawlJobController.CURRENT_DEPTH_TEXT_PROPERTY: {
                String newStringValue = evt.getNewValue().toString();
                if (!currentDepthShowLabel.getText().equals(newStringValue)) {
                    currentDepthShowLabel.setText(newStringValue);
                }
                break;
            }
            case CrawlJobController.JOB_TIMER_TEXT_PROPERTY: {
                String newStringValue = evt.getNewValue().toString();
                if (!timerShowLabel.getText().equals(newStringValue)) {
                    timerShowLabel.setText(newStringValue);
                }
                break;
            }
            case CrawlJobController.RUNNING_FUNCTION_TEXT_PROPERTY: {
                String newStringValue = evt.getNewValue().toString();
                if (!runningFunctionShowLabel.getText().equals(newStringValue)) {
                    runningFunctionShowLabel.setText(newStringValue);
                }
                break;
            }
        }
    }

    @Override
    public AbstractController getController()
    {
        return controller;
    }

    private class UpdateTimerLabel extends Timer implements ActionListener
    {

        UpdateTimerLabel()
        {
            super(UPDATE_INTERVAL, null);
            super.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            long executionTime = job.getJob().getTimer().getExecutionTime();
            long days = TimeUnit.MILLISECONDS.toDays(executionTime);
            long hours = TimeUnit.MILLISECONDS.toHours(executionTime) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(executionTime));
            long minutes = TimeUnit.MILLISECONDS.toMinutes(executionTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(executionTime));
            long seconds = TimeUnit.MILLISECONDS.toSeconds(executionTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(executionTime));

            controller.changeJobTimer(String.format("%03d : %02d : %02d : %02d", days, hours, minutes, seconds));
        }
    }

    private class UpdateRunningFunctionLabel extends Timer implements ActionListener
    {

        UpdateRunningFunctionLabel()
        {
            super(UPDATE_INTERVAL, null);
            super.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            controller.changeRunningFunction(job.getJob().getCurrentRunningFunction());
        }
    }

    private class UpdateDepthLabel extends Timer implements ActionListener
    {

        UpdateDepthLabel()
        {
            super(UPDATE_INTERVAL, null);
            super.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            controller.changeCurrentDepth(String.valueOf(job.getJob().getCurrentDepth()));
        }
    }

    private class UpdateProgress extends Timer implements ActionListener
    {

        UpdateProgress()
        {
            super(UPDATE_INTERVAL, null);
            super.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            controller.changeProgress(Integer.toString(job.getProgress()));
        }
    }

    private class CrawlJobViewPanelEvt implements ActionListener, KeyListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() == launchJobButton) {
                // Disable launch button.
                launchJobButton.setEnabled(false);
                // Control that fields aren't empty.
                if (depthTextField.getText().equalsIgnoreCase("")) {
                    JOptionPane.showMessageDialog(
                            launchJobButton,
                            AIMEConstants.EMPTY_DEPTH_FIELD_MSG.getStringConstant(),
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    depthTextField.requestFocus();
                    launchJobButton.setEnabled(true); // Enable launch button again.
                    return;
                }

                new Thread()
                {
                    @Override
                    public void run()
                    {
                        timer1.start(); // Start the counter.
                        boolean result = false;
                        try {
                            result = TasksHandler.getInstance().submitFiniteTask(job = RunCrawlJobTask.newBuild().setJob(MetadataCrawlJob.Data.newBuild()).setDepth(depthTextField.getText()).setSendmail(sendReportsCheckBox.isSelected())).get();
                        }
                        catch (InterruptedException | ExecutionException ex) {
                            JOptionPane.showMessageDialog(
                                    launchJobButton,
                                    HtmlMessageBuilder.buildHTMLMsg(
                                            "<p>Error running crawl job!</p>"
                                            + "<p>" + ex.toString() + "</p>"),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                        timer1.stop(); // Stop the counter.
                        // Present the use with a message stating the job was done, only if the job wasn't cancelled nor interrupted
                        // by any anomaly.
                        if (result) {
                            JOptionPane.showMessageDialog(
                                    launchJobButton,
                                    AIMEConstants.JOB_FINISH_MSG.getStringConstant(),
                                    "Information",
                                    JOptionPane.INFORMATION_MESSAGE);
                            launchJobButton.setEnabled(true); // Enable launch button again.
                        }
                        interrupt();
                    }
                }.start();
            }
            else if (e.getSource() == pauseButton) {
                // Logic:
                // 1. If the job is not running, issue an alert.
                if (!job.getJob().getRunning()) {
                    JOptionPane.showMessageDialog(
                            pauseButton,
                            AIMEConstants.NO_RUNNING_JOBS_MSG.getStringConstant(),
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // 2. If all passed, then pause the job, and stop the timer updater.
                job.getJob().pause();
                timer1.stop();
                JOptionPane.showMessageDialog(
                        pauseButton,
                        AIMEConstants.JOB_PAUSED_MSG.getStringConstant(),
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            else if (e.getSource() == exitButton) {
                // Logic:
                // 1. If there are running jobs.
                if (job.getJob().getRunning()) {
                    JOptionPane.showMessageDialog(
                            exitButton,
                            AIMEConstants.JOB_RUNNING_EXIT_MSG.getStringConstant(),
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // 2. Dispose the window and start the notification of job still running.
                CrawlJobWindow.getInstance().dispose();
            }
            else if (e.getSource() == resumeButton) {
                // Logic:
                // 1. If the job is already running, then issue a warning.
                if (job.getJob().getRunning()) {
                    JOptionPane.showMessageDialog(
                            resumeButton,
                            AIMEConstants.JOB_IS_RUNNING_MSG.getStringConstant(),
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 2. If the job was not started, then issue a warning.
                if (!job.getJob().getStarted()) {
                    JOptionPane.showMessageDialog(
                            resumeButton,
                            AIMEConstants.JOB_NOT_STARTED_MSG.getStringConstant(),
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 3. If all passed, then resume the job and start the updaters.
                job.getJob().resume();
                timer1.start();
                JOptionPane.showMessageDialog(
                        resumeButton,
                        AIMEConstants.JOB_RESUMED_MSG.getStringConstant(),
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }

        @Override
        public void keyTyped(KeyEvent e)
        {
        }

        @Override
        public void keyPressed(KeyEvent e)
        {
        }

        @Override
        public void keyReleased(KeyEvent e)
        {
            if (e.getSource() == depthTextField) {
                if (!NumberUtils.isDigits(depthTextField.getText()) && depthTextField.getText().length() > 0) {
                    depthTextField.setText(depthTextField.getText().substring(0, depthTextField.getText().length() - 1)); // Don't add!
                    JOptionPane.showMessageDialog(
                            depthTextField,
                            AIMEConstants.ONLY_NUMERIC_CHARS_MSG.getStringConstant(),
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        executionTimerPanel = new javax.swing.JPanel();
        timerShowLabel = new javax.swing.JLabel();
        mainPanel = new javax.swing.JPanel();
        paramsPanel = new javax.swing.JPanel();
        depthLabel = new javax.swing.JLabel();
        depthTextField = new javax.swing.JTextField();
        sendReportsLabel = new javax.swing.JLabel();
        sendReportsCheckBox = new javax.swing.JCheckBox();
        buttonsPanel = new javax.swing.JPanel();
        launchJobButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        resumeButton = new javax.swing.JButton();
        exitButton = new javax.swing.JButton();
        optionsPanel = new javax.swing.JPanel();
        crawlOption1CheckBox = new javax.swing.JCheckBox();
        indicatorsPanel = new javax.swing.JPanel();
        runningFunctionLabel = new javax.swing.JLabel();
        runningFunctionShowLabel = new javax.swing.JLabel();
        currentDepthLabel = new javax.swing.JLabel();
        currentDepthShowLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout(2, 2));

        executionTimerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Timer"));
        executionTimerPanel.setName("executionTimerPanel"); // NOI18N
        executionTimerPanel.setLayout(new java.awt.BorderLayout());

        timerShowLabel.setFont(Fonts.getInstance().getDefaultBold(20));
        timerShowLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        timerShowLabel.setText("000 : 00 : 00 : 00");
        timerShowLabel.setName("timerShowLabel"); // NOI18N
        executionTimerPanel.add(timerShowLabel, java.awt.BorderLayout.CENTER);

        add(executionTimerPanel, java.awt.BorderLayout.PAGE_START);

        mainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameters"));
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new java.awt.GridLayout(2, 1));

        paramsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
        paramsPanel.setName("paramsPanel"); // NOI18N
        paramsPanel.setLayout(new java.awt.GridLayout(2, 2, 6, 0));

        depthLabel.setText("Depth:");
        depthLabel.setName("depthLabel"); // NOI18N
        paramsPanel.add(depthLabel);

        depthTextField.setName("depthTextField"); // NOI18N
        depthTextField.addKeyListener(new CrawlJobViewPanelEvt());
        paramsPanel.add(depthTextField);

        sendReportsLabel.setText("Send Reports...?");
        sendReportsLabel.setName("sendReportsLabel"); // NOI18N
        paramsPanel.add(sendReportsLabel);

        sendReportsCheckBox.setText("Yes");
        sendReportsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sendReportsCheckBox.setName("sendReportsCheckBox"); // NOI18N
        paramsPanel.add(sendReportsCheckBox);

        mainPanel.add(paramsPanel);

        buttonsPanel.setName("buttonsPanel"); // NOI18N
        buttonsPanel.setLayout(new java.awt.GridLayout(2, 2, 4, 4));

        launchJobButton.setText("Launch");
        launchJobButton.setName("launchJobButton"); // NOI18N
        launchJobButton.addActionListener(new CrawlJobViewPanelEvt());
        buttonsPanel.add(launchJobButton);

        pauseButton.setText("Pause Job");
        pauseButton.setName("pauseButton"); // NOI18N
        pauseButton.addActionListener(new CrawlJobViewPanelEvt());
        buttonsPanel.add(pauseButton);

        resumeButton.setText("Resume Job");
        resumeButton.setName("resumeButton"); // NOI18N
        resumeButton.addActionListener(new CrawlJobViewPanelEvt());
        buttonsPanel.add(resumeButton);

        exitButton.setText("Exit");
        exitButton.setName("exitButton"); // NOI18N
        exitButton.addActionListener(new CrawlJobViewPanelEvt());
        buttonsPanel.add(exitButton);

        mainPanel.add(buttonsPanel);

        add(mainPanel, java.awt.BorderLayout.CENTER);

        optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Options"));
        optionsPanel.setName("optionsPanel"); // NOI18N

        crawlOption1CheckBox.setSelected(true);
        crawlOption1CheckBox.setText("Content Blocks");
        crawlOption1CheckBox.setName("crawlOption1CheckBox"); // NOI18N

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(crawlOption1CheckBox)
                .addGap(0, 0, 0))
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addComponent(crawlOption1CheckBox)
                .addGap(0, 113, Short.MAX_VALUE))
        );

        add(optionsPanel, java.awt.BorderLayout.EAST);

        indicatorsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Info"));
        indicatorsPanel.setName("indicatorsPanel"); // NOI18N

        runningFunctionLabel.setText("Running Function:");
        runningFunctionLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        runningFunctionLabel.setName("runningFunctionLabel"); // NOI18N

        runningFunctionShowLabel.setText("none");
        runningFunctionShowLabel.setName("runningFunctionShowLabel"); // NOI18N

        currentDepthLabel.setText("Current Depth:");
        currentDepthLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        currentDepthLabel.setName("currentDepthLabel"); // NOI18N

        currentDepthShowLabel.setText("0");
        currentDepthShowLabel.setName("currentDepthShowLabel"); // NOI18N

        javax.swing.GroupLayout indicatorsPanelLayout = new javax.swing.GroupLayout(indicatorsPanel);
        indicatorsPanel.setLayout(indicatorsPanelLayout);
        indicatorsPanelLayout.setHorizontalGroup(
            indicatorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(indicatorsPanelLayout.createSequentialGroup()
                .addGroup(indicatorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(currentDepthLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(runningFunctionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(indicatorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(runningFunctionShowLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                    .addComponent(currentDepthShowLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(69, 69, 69))
        );
        indicatorsPanelLayout.setVerticalGroup(
            indicatorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(indicatorsPanelLayout.createSequentialGroup()
                .addGroup(indicatorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runningFunctionLabel)
                    .addComponent(runningFunctionShowLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(indicatorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(currentDepthLabel)
                    .addComponent(currentDepthShowLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        add(indicatorsPanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JCheckBox crawlOption1CheckBox;
    private javax.swing.JLabel currentDepthLabel;
    private javax.swing.JLabel currentDepthShowLabel;
    private javax.swing.JLabel depthLabel;
    private javax.swing.JTextField depthTextField;
    private javax.swing.JPanel executionTimerPanel;
    private javax.swing.JButton exitButton;
    private javax.swing.JPanel indicatorsPanel;
    private javax.swing.JButton launchJobButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JPanel paramsPanel;
    private javax.swing.JButton pauseButton;
    private javax.swing.JButton resumeButton;
    private javax.swing.JLabel runningFunctionLabel;
    private javax.swing.JLabel runningFunctionShowLabel;
    private javax.swing.JCheckBox sendReportsCheckBox;
    private javax.swing.JLabel sendReportsLabel;
    private javax.swing.JLabel timerShowLabel;
    // End of variables declaration//GEN-END:variables
}
