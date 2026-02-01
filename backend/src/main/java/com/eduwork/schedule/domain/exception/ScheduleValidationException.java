package com.eduwork.schedule.domain.exception;

/**
 * Base exception for all schedule-related domain validation errors.
 */
public class ScheduleValidationException extends RuntimeException {

    public ScheduleValidationException(String message) {
        super(message);
    }

    public ScheduleValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
