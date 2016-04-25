package com.booking.service.sample;

import org.activiti.engine.*;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import junit.framework.AssertionFailedError;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.junit.After;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:com/booking/service/sample-spring-context.xml")
public class TransactionTest {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ProcessEngineConfigurationImpl processEngineConfiguration;

    @After
    public void closeProcessEngine() {
        
        processEngine.close();
    }

    @Test
    public void testSimpleCaseTxSuccessful() {

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

        // after the process is started, we have compensate event subscriptions:
        assertEquals(5, createEventSubscriptionQuery().eventType("compensate").activityId("undoBookHotel").count());
        assertEquals(1, createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());

        // the task is present:
        Task task = taskService.createTaskQuery().singleResult();
        assertNotNull(task);

        // making the tx succeed:
        taskService.setVariable(task.getId(), "confirmed", true);
        taskService.complete(task.getId());

        // now the process instance execution is sitting in the 'afterSuccess' task
        // -> has left the transaction using the "normal" sequence flow
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
        assertTrue(activeActivityIds.contains("afterSuccess"));

        // there is a compensate event subscription for the transaction under the process instance
        EventSubscriptionEntity eventSubscriptionEntity = createEventSubscriptionQuery().eventType("compensate").activityId("tx").executionId(processInstance.getId()).singleResult();

        // there is an event-scope execution associated with the event-subscription:
        assertNotNull(eventSubscriptionEntity.getConfiguration());
        Execution eventScopeExecution = runtimeService.createExecutionQuery().executionId(eventSubscriptionEntity.getConfiguration()).singleResult();
        assertNotNull(eventScopeExecution);

        // we still have compensate event subscriptions for the compensation handlers, only now they are part of the event scope
        assertEquals(5, createEventSubscriptionQuery().eventType("compensate").activityId("undoBookHotel").executionId(eventScopeExecution.getId()).count());
        assertEquals(1, createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").executionId(eventScopeExecution.getId()).count());
        assertEquals(1, createEventSubscriptionQuery().eventType("compensate").activityId("undoChargeCard").executionId(eventScopeExecution.getId()).count());

        // assert that the compensation handlers have not been invoked:
        assertNull(runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
        assertNull(runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));
        assertNull(runtimeService.getVariable(processInstance.getId(), "undoChargeCard"));

        // end the process instance
        runtimeService.signal(processInstance.getId());
        assertProcessEnded(processInstance.getId());
    }

    @Test
    public void testSimpleCaseTxCancelled() {

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("transactionProcess");

        // after the process is started, we have compensate event subscriptions:
        assertEquals(5, createEventSubscriptionQuery().eventType("compensate").activityId("undoBookHotel").count());
        assertEquals(1, createEventSubscriptionQuery().eventType("compensate").activityId("undoBookFlight").count());

        // the task is present:
        Task task = taskService.createTaskQuery().singleResult();
        assertNotNull(task);

        // making the tx fail:
        taskService.setVariable(task.getId(), "confirmed", false);
        taskService.complete(task.getId());

        // now the process instance execution is sitting in the 'afterCancellation' task
        // -> has left the transaction using the cancel boundary event
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstance.getId());
        assertTrue(activeActivityIds.contains("afterCancellation"));

        // we have no more compensate event subscriptions
        assertEquals(0, createEventSubscriptionQuery().eventType("compensate").count());

        // assert that the compensation handlers have been invoked:
        assertEquals(5, runtimeService.getVariable(processInstance.getId(), "undoBookHotel"));
        assertEquals(1, runtimeService.getVariable(processInstance.getId(), "undoBookFlight"));
        assertEquals(1, runtimeService.getVariable(processInstance.getId(), "undoChargeCard"));

        // if we have history, we check that the invocation of the compensation handlers is recorded in history.
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            assertEquals(1, historyService.createHistoricActivityInstanceQuery()
                    .activityId("undoBookFlight")
                    .count());

            assertEquals(5, historyService.createHistoricActivityInstanceQuery()
                    .activityId("undoBookHotel")
                    .count());

            assertEquals(1, historyService.createHistoricActivityInstanceQuery()
                    .activityId("undoChargeCard")
                    .count());
        }

        // end the process instance
        runtimeService.signal(processInstance.getId());
        assertProcessEnded(processInstance.getId());
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
