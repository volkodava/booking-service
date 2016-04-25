package com.booking.service.sample.activity;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

public class UndoService implements JavaDelegate {

    private Expression counterName;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String variableName = (String) counterName.getValue(execution);
        Object variable = execution.getVariable(variableName);
        if (variable == null) {
            execution.setVariable(variableName, 1);
        } else {
            execution.setVariable(variableName, ((Integer) variable) + 1);
        }
    }
}
