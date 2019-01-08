package io.aime.mvc.model;

import io.aime.mvc.controller.CrawlJobController;
import net.apkc.emma.mvc.AbstractModel;

public final class CrawlJobModel extends AbstractModel
{

    private String runningFunction;
    private String currentDepth;
    private String jobTimer;
    private String progress;

    public String getRunningFunction()
    {
        return runningFunction;
    }

    public String getCurrentDepth()
    {
        return currentDepth;
    }

    public String getJobTimer()
    {
        return jobTimer;
    }

    public String getProgress()
    {
        return progress;
    }

    public void setRunningFunction(String newRunningFunction)
    {
        String oldRunningFunction = runningFunction;
        runningFunction = newRunningFunction;
        firePropertyChange(CrawlJobController.RUNNING_FUNCTION_TEXT_PROPERTY, oldRunningFunction, newRunningFunction);
    }

    public void setCurrentDepth(String newCurrentDepth)
    {
        String oldCurrentDepth = currentDepth;
        currentDepth = newCurrentDepth;
        firePropertyChange(CrawlJobController.CURRENT_DEPTH_TEXT_PROPERTY, oldCurrentDepth, newCurrentDepth);
    }

    public void setJobTimer(String newJobTimer)
    {
        String oldJobTimer = jobTimer;
        jobTimer = newJobTimer;
        firePropertyChange(CrawlJobController.JOB_TIMER_TEXT_PROPERTY, oldJobTimer, newJobTimer);
    }

    public void setProgress(String newProgress)
    {
        String oldProgress = progress;
        progress = newProgress;
        firePropertyChange(CrawlJobController.PROGRESS_PROPERTY, oldProgress, newProgress);
    }
}
