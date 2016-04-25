package com.booking.service.activity;

import java.util.Map;
import java.util.Map.Entry;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerActivity {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void log(ActivityExecution execution, String name) {
        logger.info("\n[***] {} called", name);
        dumpAllVars(execution);
    }

    private void dumpAllVars(ActivityExecution execution) {
        Map<String, Object> variables = execution.getVariables();
        for (Entry<String, Object> var : variables.entrySet()) {
            logger.info("\n[***] {} / {}", var.getKey(), var.getValue());
        }
    }
}
