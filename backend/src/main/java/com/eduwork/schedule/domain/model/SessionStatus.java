package com.eduwork.schedule.domain.model;

/**
 * SessionStatus represents the lifecycle states of a bookable Session.
 * 
 * State Transitions:
 * DRAFT → OPEN → WAITING/CONFIRMED → IN_PROGRESS → COMPLETED → REVIEWED
 * ↓
 * CANCELLED
 *
 * Business Rules:
 * - DRAFT: Not yet published, mentor can edit freely
 * - OPEN: Published and accepting bookings
 * - WAITING: GROUP session with bookings below minimum
 * - CONFIRMED: Minimum reached (GROUP) or booked (ONE_ON_ONE), will proceed
 * - IN_PROGRESS: Currently happening
 * - COMPLETED: Finished
 * - REVIEWED: Students have submitted reviews
 * - CANCELLED: Cancelled by mentor or system
 */
public enum SessionStatus {
    /**
     * Session is being edited by mentor, not yet published.
     */
    DRAFT("Draft", "Not yet published"),

    /**
     * Session is published and actively accepting bookings.
     */
    OPEN("Open for Booking", "Accepting bookings"),

    /**
     * GROUP session has bookings but below minimum students.
     * Waiting for more students or mentor decision.
     */
    WAITING("Waiting for Minimum", "Below minimum students"),

    /**
     * Session will proceed:
     * - GROUP: Minimum students reached
     * - ONE_ON_ONE: Booked
     */
    CONFIRMED("Confirmed", "Will proceed as scheduled"),

    /**
     * Session is currently in progress.
     */
    IN_PROGRESS("In Progress", "Currently happening"),

    /**
     * Session has finished.
     */
    COMPLETED("Completed", "Finished"),

    /**
     * Students have submitted reviews for this session.
     */
    REVIEWED("Reviewed", "Reviews submitted"),

    /**
     * Session was cancelled (by mentor, student, or system).
     */
    CANCELLED("Cancelled", "Cancelled");

    private final String displayName;
    private final String description;

    SessionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if bookings are allowed in this status.
     */
    public boolean canAcceptBookings() {
        return this == OPEN;
    }

    /**
     * Check if session is in active lifecycle (not terminal).
     */
    public boolean isActive() {
        return this != CANCELLED && this != COMPLETED && this != REVIEWED;
    }

    /**
     * Check if session is terminal (no further transitions).
     */
    public boolean isTerminal() {
        return this == CANCELLED || this == REVIEWED;
    }
}
