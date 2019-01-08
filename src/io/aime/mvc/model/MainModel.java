package io.aime.mvc.model;

import io.aime.mvc.controller.MainController;
import net.apkc.emma.mvc.AbstractModel;

public final class MainModel extends AbstractModel
{

    private String mainDBase;
    private String hadoopFiles;
    private String logs;
    private String kernelIndex;
    private String kernelData;
    private String cycleComplete;
    private String completedIterations;
    private String lastFunction;
    private String timeLastFunction;
    private String executionType;
    private String heapSize;
    private String botConsoleQueueDeep;

    public void setMainDBase(String newMainDBase)
    {
        String oldMainDBase = mainDBase;
        mainDBase = newMainDBase;
        firePropertyChange(MainController.MAIN_DBASE_TEXT_PROPERTY, oldMainDBase, newMainDBase);
    }

    public void setHadoopFiles(String newHadoopFiles)
    {
        String oldHadoopFiles = hadoopFiles;
        hadoopFiles = newHadoopFiles;
        firePropertyChange(MainController.HADOOP_FILES_TEXT_PROPERTY, oldHadoopFiles, newHadoopFiles);
    }

    public void setLogs(String newLogs)
    {
        String oldLogs = logs;
        logs = newLogs;
        firePropertyChange(MainController.LOGS_TEXT_PROPERTY, oldLogs, newLogs);
    }

    public void setKernelIndex(String newKernelIndex)
    {
        String oldKernelIndex = kernelIndex;
        kernelIndex = newKernelIndex;
        firePropertyChange(MainController.KERNEL_INDEX_TEXT_PROPERTY, oldKernelIndex, newKernelIndex);
    }

    public void setKernelData(String newKernelData)
    {
        String oldKernelData = kernelData;
        kernelData = newKernelData;
        firePropertyChange(MainController.KERNEL_DATA_TEXT_PROPERTY, oldKernelData, newKernelData);
    }

    public void setCycleComplete(String newCycleComplete)
    {
        String oldCycleComplete = cycleComplete;
        cycleComplete = newCycleComplete;
        firePropertyChange(MainController.CYCLE_COMPLETE_TEXT_PROPERTY, oldCycleComplete, newCycleComplete);
    }

    public void setCompletedIterations(String newCompletedIterations)
    {
        String oldCompletedIterations = completedIterations;
        completedIterations = newCompletedIterations;
        firePropertyChange(MainController.COMPLETED_ITERATIONS_TEXT_PROPERTY, oldCompletedIterations, newCompletedIterations);
    }

    public void setLastFunction(String newLastFunction)
    {
        String oldLastFunction = lastFunction;
        lastFunction = newLastFunction;
        firePropertyChange(MainController.LAST_FUNCTION_TEXT_PROPERTY, oldLastFunction, newLastFunction);
    }

    public void setTimeLastFunction(String newTimeLastFunction)
    {
        String oldTimeLastFunction = timeLastFunction;
        timeLastFunction = newTimeLastFunction;
        firePropertyChange(MainController.TIME_LAST_FUNCTION_TEXT_PROPERTY, oldTimeLastFunction, newTimeLastFunction);
    }

    public void setExecutionType(String newExecutionType)
    {
        String oldExecutionType = executionType;
        executionType = newExecutionType;
        firePropertyChange(MainController.EXECUTION_TYPE_TEXT_PROPERTY, oldExecutionType, newExecutionType);
    }

    public void setHeapSize(String newHeapSize)
    {
        String oldHeapSize = heapSize;
        heapSize = newHeapSize;
        firePropertyChange(MainController.HEAP_SIZE_TEXT_PROPERTY, oldHeapSize, newHeapSize);
    }

    public void setBotConsoleQueueDeep(String newDeep)
    {
        String oldDeep = botConsoleQueueDeep;
        botConsoleQueueDeep = newDeep;
        firePropertyChange(MainController.BOT_CONSOLE_QUEUE_DEEP_TEXT_PROPERTY, oldDeep, newDeep);
    }
}
