package com.eduwork.identity.domain.service;

/**
 * Domain service for brute force protection.
 * Tracks login attempts and manages account locking to prevent
 * automated attacks, credential stuffing, and password guessing.
 */
public interface BruteForceProtectionService {

    /**
     * Records a failed login attempt for the given email and IP address.
     * Increments attempt counters and may trigger account locking.
     *
     * @param email     the email address used in the failed attempt
     * @param ipAddress the IP address of the failed attempt
     */
    void recordFailedAttempt(String email, String ipAddress);

    /**
     * Records a successful login.
     * Resets the failed attempt counter for the email.
     *
     * @param email     the email address that logged in successfully
     * @param ipAddress the IP address of the successful login
     */
    void recordSuccessfulLogin(String email, String ipAddress);

    /**
     * Checks if an account is currently locked due to too many failed attempts.
     *
     * @param email the email address to check
     * @return true if the account is locked, false otherwise
     */
    boolean isAccountLocked(String email);

    /**
     * Checks if an IP address is currently blocked due to too many failed attempts.
     *
     * @param ipAddress the IP address to check
     * @return true if the IP is blocked, false otherwise
     */
    boolean isIpBlocked(String ipAddress);

    /**
     * Resets the failed attempt counter for a given email.
     * Used when a user successfully logs in or when manually unlocking an account.
     *
     * @param email the email address to reset attempts for
     */
    void resetAttempts(String email);

    /**
     * Gets the current number of failed attempts for an email.
     *
     * @param email the email address to check
     * @return the number of failed attempts
     */
    int getFailedAttempts(String email);
}
