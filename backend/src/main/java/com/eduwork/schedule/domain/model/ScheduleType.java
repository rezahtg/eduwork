package com.eduwork.schedule.domain.model;

/**
 * Schedule Type Enum
 * Defines whether a schedule is one-time or recurring.
 */
public enum ScheduleType {
    /**
     * One-time schedule with specific time slots.
     */
    ONE_TIME,

    /**
     * Recurring schedule (future enhancement for calendar integration).
     * Will support weekly patterns like "Every Monday 9-11 AM".
     */
    RECURRING
}
