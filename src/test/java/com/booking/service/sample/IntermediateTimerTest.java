package com.booking.service.sample;

import com.booking.service.common.TestActivitiEntityEventListener;
import java.util.Calendar;
import java.util.List;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.test.JobTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static junit.framework.TestCase.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:com/booking/service/sample-spring-context.xml")
public class IntermediateTimerTest {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private ProcessEngineConfigurationImpl processEngineConfiguration;

    private TestActivitiEntityEventListener listener;

    @Before
    public void setUp() throws Exception {
        listener = new TestActivitiEntityEventListener();
        processEngineConfiguration.getEventDispatcher().addEventListener(listener);
    }

    @After
    public void tearDown() throws Exception {
        if (listener != null) {
            processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
        }

        processEngine.close();
    }

    @Test
    public void testTimerFiredForIntermediateTimer() throws Exception {
        runtimeService.startProcessInstanceByKey("testTimerFiredForIntermediateTimer");

        // Force timer to start the process
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        processEngineConfiguration.getClock().setCurrentTime(tomorrow.getTime());
        waitForJobExecutorToProcessAllJobs(2000, 100);

        checkEventCount(0, ActivitiEventType.JOB_CANCELED);
        checkEventCount(1, ActivitiEventType.TIMER_FIRED);
    }

    public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
        JobTestHelper.waitForJobExecutorToProcessAllJobs(processEngineConfiguration, managementService, maxMillisToWait, intervalMillis);
    }

    private void checkEventCount(int expectedCount, ActivitiEventType eventType) {
        int timerCancelledCount = 0;
        List<ActivitiEvent> eventsReceived = listener.getEventsReceived();
        for (ActivitiEvent eventReceived : eventsReceived) {
            if (eventType.equals(eventReceived.getType())) {
                timerCancelledCount++;
            }
        }
        assertEquals(eventType.name() + " event was expected " + expectedCount + " times.", expectedCount, timerCancelledCount);
    }
}
