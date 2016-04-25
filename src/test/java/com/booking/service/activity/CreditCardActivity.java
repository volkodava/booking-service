package com.booking.service.activity;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreditCardActivity {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void charge(ActivityExecution execution) {
        logger.info("\n[***] Charge called");
        execution.setVariable("charge", Boolean.TRUE);
    }
}
