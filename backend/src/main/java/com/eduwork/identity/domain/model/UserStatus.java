package com.eduwork.identity.domain.model;

/**
 * User account status lifecycle.
 * 
 * Lifecycle:
 * PENDING_VERIFICATION -> ACTIVE -> SUSPENDED -> DELETED
 */
public enum UserStatus {
    /**
     * User registered but email not verified yet.
     * Cannot login until verified.
     */
    PENDING_VERIFICATION,

    /**
     * Email verified, account active.
     * Can login and use platform.
     */
    ACTIVE,

    /**
     * Account temporarily suspended by admin.
     * Cannot login until reactivated.
     */
    SUSPENDED,

    /**
     * Account soft-deleted.
     * Data retained for auditing.
     */
    DELETED
}
