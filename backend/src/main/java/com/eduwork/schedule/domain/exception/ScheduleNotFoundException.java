package com.eduwork.schedule.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a schedule is not found.
 */
public class ScheduleNotFoundException extends RuntimeException {
    public ScheduleNotFoundException(String message) {
        super(message);
    }

    public ScheduleNotFoundException(UUID scheduleId) {
        super("Schedule not found with ID: " + scheduleId);
    }
}
