package com.eduwork.identity.domain.model;

/**
 * User role in the platform.
 * A user can have multiple roles.
 */
public enum UserRole {
    /**
     * Student - can search schedules and book sessions.
     */
    STUDENT,

    /**
     * Mentor - can create schedules and teach sessions.
     */
    MENTOR,

    /**
     * Admin - platform administrator with full access.
     */
    ADMIN
}
