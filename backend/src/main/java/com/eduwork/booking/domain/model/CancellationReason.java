package com.eduwork.booking.domain.model;

/**
 * CancellationReason indicates why a booking was cancelled.
 * Used for analytics and refund policy application.
 */
public enum CancellationReason {
    /**
     * Student voluntarily cancelled the booking.
     * Refund policy applies based on timing.
     */
    STUDENT_REQUEST("Student Request", "Student cancelled"),

    /**
     * Mentor cancelled the session.
     * Full refund to student.
     */
    MENTOR_CANCELLED("Mentor Cancelled", "Mentor cancelled session"),

    /**
     * Payment deadline (24h) expired.
     * No refund (nothing was paid).
     */
    PAYMENT_TIMEOUT("Payment Timeout", "Payment deadline expired"),

    /**
     * GROUP session minimum students not met by deadline.
     * Full refund to all students.
     */
    MINIMUM_NOT_MET("Minimum Not Met", "Below minimum students"),

    /**
     * Emergency situation during session.
     * Requires admin review for refund.
     */
    EMERGENCY("Emergency", "Emergency situation"),

    /**
     * Admin manually cancelled (dispute resolution, policy violation, etc).
     * Refund determined by admin.
     */
    ADMIN_ACTION("Admin Action", "Cancelled by administrator");

    private final String displayName;
    private final String description;

    CancellationReason(String displayName, String description) {
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
     * Check if this cancellation type qualifies for automatic refund.
     */
    public boolean isAutoRefundable() {
        return this == MENTOR_CANCELLED || this == MINIMUM_NOT_MET;
    }

    /**
     * Check if this cancellation requires admin review.
     */
    public boolean requiresAdminReview() {
        return this == EMERGENCY;
    }
}
