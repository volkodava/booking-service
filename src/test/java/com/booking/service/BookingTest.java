package com.booking.service;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import java.util.Calendar;

@ContextConfiguration("classpath:com/booking/service/spring-context.xml")
public class BookingTest extends BaseTest {

    @Test
    @Deployment(resources = {"com/booking/service/autodeploy/booking.bpmn20.xml"})
    public void testBookingProcessWhenMessageReceived() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("bookingProcess");

        // verify a job is created for process definition
        assertEquals(1, managementService.createJobQuery().count());

        // verify the process instance has a compensate and message event subscriptions
        assertEquals(1,
                createEventSubscriptionQuery().eventType("compensate").activityId("cancelHotelReservation").count());
        EventSubscriptionQueryImpl confirmationEventSubscription = createEventSubscriptionQuery().
                eventType("message").activityId("receiveConfirmationEvent");
        assertEquals(1, confirmationEventSubscription.count());

        // verify query for an execution wich has message subscription
        Execution execution = runtimeService.createExecutionQuery()
                .messageEventSubscriptionName("receivedBookConfirmationMessage").singleResult();
        assertNotNull(execution);

        // verify flow execution
        assertEquals(true, runtimeService.getVariable(processInstance.getId(), "bookHotel"));
        assertNull(runtimeService.getVariable(processInstance.getId(), "cancelHotelReservation"));

        EventSubscriptionEntity messageEventSubscription = confirmationEventSubscription.singleResult();
        runtimeService.messageEventReceived(messageEventSubscription.getEventName(),
                messageEventSubscription.getExecutionId());

        // verify process instance completed
        assertProcessEnded(processInstance.getId());
        checkEventCount(1, ActivitiEventType.PROCESS_COMPLETED);
        assertEquals(0, createEventSubscriptionQuery().count());
        assertEquals(0, managementService.createJobQuery().count());

        // verify no compensation handlers is recorded in history
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            assertEquals(0, historyService.createHistoricActivityInstanceQuery()
                    .activityId("cancelHotelReservation").count());
        }
    }

    @Test
    @Deployment(resources = {"com/booking/service/autodeploy/booking.bpmn20.xml"})
    public void testBookingProcessWhenTimeoutFired() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("bookingProcess");

        // verify a job is created for process definition
        assertEquals(1, managementService.createJobQuery().count());

        // verify the process instance has a compensate and message event subscriptions
        assertEquals(1,
                createEventSubscriptionQuery().eventType("compensate").activityId("cancelHotelReservation").count());
        EventSubscriptionQueryImpl confirmationEventSubscription = createEventSubscriptionQuery().
                eventType("message").activityId("receiveConfirmationEvent");
        assertEquals(1, confirmationEventSubscription.count());

        // verify query for an execution wich has message subscription
        Execution execution = runtimeService.createExecutionQuery()
                .messageEventSubscriptionName("receivedBookConfirmationMessage").singleResult();
        assertNotNull(execution);

        // verify flow execution
        assertEquals(true, runtimeService.getVariable(processInstance.getId(), "bookHotel"));
        assertNull(runtimeService.getVariable(processInstance.getId(), "cancelHotelReservation"));

        // update internal time to simulate timeout
        Calendar elevenMinutesAfter = Calendar.getInstance();
        elevenMinutesAfter.add(Calendar.MINUTE, 11);
        processEngineConfiguration.getClock().setCurrentTime(elevenMinutesAfter.getTime());

        waitForJobExecutorToProcessAllJobs(10000, 100);

        // verify process instance completed
        assertProcessEnded(processInstance.getId());
        checkEventCount(1, ActivitiEventType.PROCESS_COMPLETED);
        assertEquals(0, createEventSubscriptionQuery().count());
        assertEquals(0, managementService.createJobQuery().count());

        // verify compensation handlers is recorded in history
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            assertEquals(1, historyService.createHistoricActivityInstanceQuery()
                    .activityId("cancelHotelReservation").count());
        }
    }
}
