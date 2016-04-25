package com.booking.service.common;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;

public class TestActivitiEntityEventListener implements ActivitiEventListener {

    private List<ActivitiEvent> eventsReceived;

    public TestActivitiEntityEventListener() {
        eventsReceived = new ArrayList<ActivitiEvent>();
    }

    public List<ActivitiEvent> getEventsReceived() {
        return eventsReceived;
    }

    public void clearEventsReceived() {
        eventsReceived.clear();
    }

    @Override
    public void onEvent(ActivitiEvent event) {
        if (event instanceof ActivitiEntityEvent
                && Job.class.isAssignableFrom(((ActivitiEntityEvent) event).getEntity().getClass())) {
            eventsReceived.add(event);
        } else if (event instanceof ActivitiEntityEvent
                && ProcessInstance.class.isAssignableFrom(((ActivitiEntityEvent) event).getEntity().getClass())) {
            eventsReceived.add(event);
        }
    }

    @Override
    public boolean isFailOnException() {
        return true;
    }

    public List<ActivitiEvent> filterEvents(ActivitiEventType eventType) {// count timer cancelled events
        List<ActivitiEvent> filteredEvents = new ArrayList<ActivitiEvent>();
        for (ActivitiEvent eventReceived : eventsReceived) {
            if (eventType.equals(eventReceived.getType())) {
                filteredEvents.add(eventReceived);
            }
        }
        return filteredEvents;
    }
}
