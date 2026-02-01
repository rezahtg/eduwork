package com.eduwork.schedule.domain.model;

/**
 * Schedule Status Enum
 * Represents the lifecycle status of a schedule.
 */
public enum ScheduleStatus {
    /**
     * Schedule is being created/edited, not visible to students.
     */
    DRAFT,

    /**
     * Schedule is published and visible to students for booking.
     */
    PUBLISHED,

    /**
     * Schedule has been cancelled and is no longer available.
     */
    CANCELLED
}
