package com.booking.service.activity;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookHotelActivity {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void bookHotel(ActivityExecution execution) {
        logger.info("\n[***] Book hotel called");
        execution.setVariable("bookHotel", Boolean.TRUE);
    }

    public void cancelReservation(ActivityExecution execution) {
        logger.info("\n[***] Cancel hotel reservation called");
        execution.setVariable("cancelReservation", Boolean.TRUE);
    }
}
