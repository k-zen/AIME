package io.aime.mvc.controller;

import net.apkc.emma.mvc.AbstractController;

public final class MainController extends AbstractController
{

    public static final String MAIN_DBASE_TEXT_PROPERTY = "MainDBase";
    public static final String HADOOP_FILES_TEXT_PROPERTY = "HadoopFiles";
    public static final String LOGS_TEXT_PROPERTY = "Logs";
    public static final String KERNEL_INDEX_TEXT_PROPERTY = "KernelIndex";
    public static final String KERNEL_DATA_TEXT_PROPERTY = "KernelData";
    public static final String CYCLE_COMPLETE_TEXT_PROPERTY = "CycleComplete";
    public static final String COMPLETED_ITERATIONS_TEXT_PROPERTY = "CompletedIterations";
    public static final String LAST_FUNCTION_TEXT_PROPERTY = "LastFunction";
    public static final String TIME_LAST_FUNCTION_TEXT_PROPERTY = "TimeLastFunction";
    public static final String EXECUTION_TYPE_TEXT_PROPERTY = "ExecutionType";
    public static final String HEAP_SIZE_TEXT_PROPERTY = "HeapSize";
    public static final String BOT_CONSOLE_QUEUE_DEEP_TEXT_PROPERTY = "BotConsoleQueueDeep";

    public void changeMainDBase(String newText)
    {
        setModelProperty(MAIN_DBASE_TEXT_PROPERTY, newText);
    }

    public void changeHadoopFiles(String newText)
    {
        setModelProperty(HADOOP_FILES_TEXT_PROPERTY, newText);
    }

    public void changeLogs(String newText)
    {
        setModelProperty(LOGS_TEXT_PROPERTY, newText);
    }

    public void changeKernelIndex(String newText)
    {
        setModelProperty(KERNEL_INDEX_TEXT_PROPERTY, newText);
    }

    public void changeKernelData(String newText)
    {
        setModelProperty(KERNEL_DATA_TEXT_PROPERTY, newText);
    }

    public void changeCycleComplete(String newText)
    {
        setModelProperty(CYCLE_COMPLETE_TEXT_PROPERTY, newText);
    }

    public void changeCompletedIterations(String newText)
    {
        setModelProperty(COMPLETED_ITERATIONS_TEXT_PROPERTY, newText);
    }

    public void changeLastFunction(String newText)
    {
        setModelProperty(LAST_FUNCTION_TEXT_PROPERTY, newText);
    }

    public void changeTimeLastFunction(String newText)
    {
        setModelProperty(TIME_LAST_FUNCTION_TEXT_PROPERTY, newText);
    }

    public void changeExecutionType(String newText)
    {
        setModelProperty(EXECUTION_TYPE_TEXT_PROPERTY, newText);
    }

    public void changeHeapSize(String newText)
    {
        setModelProperty(HEAP_SIZE_TEXT_PROPERTY, newText);
    }

    public void changeQueueDeepText(String newText)
    {
        setModelProperty(BOT_CONSOLE_QUEUE_DEEP_TEXT_PROPERTY, newText);
    }
}
