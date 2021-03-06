package io.aime.mvc.controller;

import net.apkc.emma.mvc.AbstractController;

public final class FilterRuleController extends AbstractController
{

    public static final String EXECUTION_RESULT_PROPERTY = "ExecutionResult";
    public static final String ELEMENT_TEXT_PROPERTY = "Text";

    public void changeExecutionResult(String newExecutionResult)
    {
        this.setModelProperty(EXECUTION_RESULT_PROPERTY, newExecutionResult);
    }

    public void changeElementText(String newText)
    {
        this.setModelProperty(ELEMENT_TEXT_PROPERTY, newText);
    }
}
