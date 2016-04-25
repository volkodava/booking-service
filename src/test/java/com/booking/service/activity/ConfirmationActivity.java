package com.booking.service.activity;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfirmationActivity {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void requestConfirmation(ActivityExecution execution) {
        logger.info("\n[***] Request confirmation called");
        execution.setVariable("requestConfirmation", Boolean.TRUE);
    }
}
