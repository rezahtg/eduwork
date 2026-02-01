package com.eduwork.schedule.domain.model;

/**
 * Time Slot Status Enum
 * Represents the booking status of an individual time slot.
 */
public enum TimeSlotStatus {
    /**
     * Time slot is available for booking.
     */
    AVAILABLE,

    /**
     * Time slot has been booked by a student.
     */
    BOOKED,

    /**
     * Time slot has been cancelled (marked as unavailable).
     */
    CANCELLED
}
