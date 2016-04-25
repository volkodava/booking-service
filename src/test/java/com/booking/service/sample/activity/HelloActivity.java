package com.booking.service.sample.activity;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

public class HelloActivity {

    public void setHelloMessage(ActivityExecution execution) {
        String name = (String) execution.getVariable("name");
        execution.setVariable("hello", String.format("Hello, %s!", name));
    }
}
