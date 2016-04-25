package com.booking.service.sample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.AssertionFailedError;
import static junit.framework.TestCase.assertEquals;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:com/booking/service/sample-spring-context.xml")
public class SalesTest {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProcessEngineConfigurationImpl processEngineConfiguration;

    @Before
    public void setUp() {
        // Normally the UI will do this automatically for us
        Authentication.setAuthenticatedUserId("kermit");
    }

    @After
    public void tearDown() {
        Authentication.setAuthenticatedUserId(null);
        
        processEngine.close();
    }

    @Test
    public void testReviewSalesLeadProcess() {

        // After starting the process, a task should be assigned to the 'initiator' (normally set by GUI)
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("details", "very interesting");
        variables.put("customerName", "Alfresco");
        String procId = runtimeService.startProcessInstanceByKey("reviewSaledLead", variables).getId();
        Task task = taskService.createTaskQuery().taskAssignee("kermit").singleResult();
        assertEquals("Provide new sales lead", task.getName());

        // After completing the task, the review subprocess will be active
        taskService.complete(task.getId());
        Task ratingTask = taskService.createTaskQuery().taskCandidateGroup("accountancy").singleResult();
        assertEquals("Review customer rating", ratingTask.getName());
        Task profitabilityTask = taskService.createTaskQuery().taskCandidateGroup("management").singleResult();
        assertEquals("Review profitability", profitabilityTask.getName());

        // Complete the management task by stating that not enough info was provided
        // This should throw the error event, which closes the subprocess
        variables = new HashMap<String, Object>();
        variables.put("notEnoughInformation", true);
        taskService.complete(profitabilityTask.getId(), variables);

        // The 'provide additional details' task should now be active
        Task provideDetailsTask = taskService.createTaskQuery().taskAssignee("kermit").singleResult();
        assertEquals("Provide additional details", provideDetailsTask.getName());

        // Providing more details (ie. completing the task), will activate the subprocess again
        taskService.complete(provideDetailsTask.getId());
        List<Task> reviewTasks = taskService.createTaskQuery().orderByTaskName().asc().list();
        assertEquals("Review customer rating", reviewTasks.get(0).getName());
        assertEquals("Review profitability", reviewTasks.get(1).getName());

        // Completing both tasks normally ends the process
        taskService.complete(reviewTasks.get(0).getId());
        variables.put("notEnoughInformation", false);
        taskService.complete(reviewTasks.get(1).getId(), variables);
        assertProcessEnded(procId);
    }

    private EventSubscriptionQueryImpl createEventSubscriptionQuery() {
        return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor());
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
