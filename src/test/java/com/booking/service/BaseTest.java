package com.booking.service;

import com.booking.service.common.TestActivitiEntityEventListener;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.apache.commons.lang3.Validate;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import static junit.framework.TestCase.assertEquals;
import org.activiti.spring.impl.test.SpringActivitiTestCase;

public abstract class BaseTest extends SpringActivitiTestCase {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    protected ProcessEngine processEngine;

    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected TaskService taskService;

    @Autowired
    protected HistoryService historyService;

    @Autowired
    protected ManagementService managementService;

    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected ProcessEngineConfigurationImpl processEngineConfiguration;

    @PostConstruct
    public void validate() {
        Validate.notNull(processEngine);
        Validate.notNull(runtimeService);
        Validate.notNull(taskService);
        Validate.notNull(historyService);
        Validate.notNull(managementService);
        Validate.notNull(repositoryService);
        Validate.notNull(processEngineConfiguration);
    }

    protected TestActivitiEntityEventListener listener;

    @Before
    public void setUp() throws Exception {
        listener = new TestActivitiEntityEventListener();
        processEngineConfiguration.getEventDispatcher().addEventListener(listener);
    }

    @After
    public void tearDown() {
        if (listener != null) {
            listener.clearEventsReceived();
            processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
        }
        processEngine.close();
        processEngineConfiguration.getClock().setCurrentTime(new Date());
    }

    protected EventSubscriptionQueryImpl createEventSubscriptionQuery() {
        return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor());
    }

    protected void checkEventCount(int expectedCount, ActivitiEventType eventType) {
        List<ActivitiEvent> filteredEvents = listener.filterEvents(eventType);
        assertEquals(eventType.name() + " event was expected " + expectedCount + " times.", expectedCount,
                filteredEvents.size());
    }
}
