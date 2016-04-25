package com.booking.service.sample;

import com.booking.service.sample.common.ProcessManagerBean;
import junit.framework.AssertionFailedError;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.After;
import org.junit.Rule;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:com/booking/service/sample-spring-context.xml")
public class HelloTest {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProcessManagerBean processManagerBean;

    @Autowired
    @Rule
    public ActivitiRule activitiSpringRule;

    @After
    public void closeProcessEngine() {

        processEngine.close();
    }

    @Test
    @Deployment(resources = {"com/booking/service/sample/autodeploy/hello.bpmn20.xml"})
    public void testHello() {
        processManagerBean.runHello(Collections.singletonMap("name", "Activiti"));

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
        assertNotNull(processInstance);
        assertEquals("Hello, Activiti!", runtimeService.getVariable(processInstance.getId(), "hello"));

        processManagerBean.completeTask(taskService.createTaskQuery().singleResult().getId());

        assertProcessEnded(processInstance.getId());
    }

    public void assertProcessEnded(final String processInstanceId) {
        ProcessInstance processInstance = processEngine
                .getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (processInstance != null) {
            throw new AssertionFailedError("Expected finished process instance '" + processInstanceId + "' but it was still in the db");
        }
    }
}
