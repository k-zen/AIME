package io.aime.mvc.model;

import io.aime.mvc.controller.FilterRuleController;
import net.apkc.emma.mvc.AbstractModel;

public final class FilterRuleModel extends AbstractModel
{

    private String executionResult;
    private String text;

    public void setExecutionResult(String executionResult)
    {
        String oldExecutionResult = this.executionResult;
        this.executionResult = executionResult;
        firePropertyChange(FilterRuleController.EXECUTION_RESULT_PROPERTY, oldExecutionResult, executionResult);
    }

    public String getExecutionResult()
    {
        return executionResult;
    }

    public void setText(String text)
    {
        String oldText = this.text;
        this.text = text;
        firePropertyChange(FilterRuleController.ELEMENT_TEXT_PROPERTY, oldText, text);
    }

    public String getText()
    {
        return text;
    }
}
