package com.booking.service.sample.common;

import java.util.Map;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

public class ProcessManagerBean {

    private RuntimeService runtimeService;

    private TaskService taskService;

    private DataSource dataSource;

    @Transactional
    public void runHello(Map<String, Object> variables) {
        runtimeService.startProcessInstanceByKey("helloProcess", variables);
    }

    @Transactional
    public void completeTask(String taskId) {
        taskService.complete(taskId);
    }

    @Required
    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Required
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Required
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
