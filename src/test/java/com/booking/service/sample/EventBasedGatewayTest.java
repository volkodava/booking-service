package com.booking.service.sample;

import java.util.Date;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;

import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:com/booking/service/sample-spring-context.xml")
public class EventBasedGatewayTest {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private ProcessEngineConfigurationImpl processEngineConfiguration;

    @After
    public void closeProcessEngine() {
        
        processEngine.close();
    }

    @Test
    public void testCatchSignalAndMessageAndTimer() {

        runtimeService.startProcessInstanceByKey("catchSignal");

        assertEquals(2, createEventSubscriptionQuery().count());
        EventSubscriptionQueryImpl messageEventSubscriptionQuery = createEventSubscriptionQuery().eventType("message");
        assertEquals(1, messageEventSubscriptionQuery.count());
        assertEquals(1, createEventSubscriptionQuery().eventType("signal").count());
        assertEquals(1, managementService.createJobQuery().count());

        // we can query for an execution with has both a signal AND message subscription
        Execution execution = runtimeService.createExecutionQuery()
                .messageEventSubscriptionName("newInvoice")
                .signalEventSubscriptionName("alert")
                .singleResult();
        assertNotNull(execution);

        processEngineConfiguration.getClock().setCurrentTime(new Date(processEngineConfiguration.getClock().getCurrentTime().getTime() + 10000));
        try {

            EventSubscriptionEntity messageEventSubscription = messageEventSubscriptionQuery.singleResult();
            runtimeService.messageEventReceived(messageEventSubscription.getEventName(), messageEventSubscription.getExecutionId());

            assertEquals(0, createEventSubscriptionQuery().count());
            assertEquals(0, managementService.createJobQuery().count());

            Task task = taskService.createTaskQuery()
                    .taskName("afterMessage")
                    .singleResult();

            assertNotNull(task);

            taskService.complete(task.getId());
        } finally {
            processEngineConfiguration.getClock().setCurrentTime(new Date());
        }
    }

    private EventSubscriptionQueryImpl createEventSubscriptionQuery() {
        return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor());
    }
}
