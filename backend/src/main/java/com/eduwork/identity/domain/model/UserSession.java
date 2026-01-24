package com.eduwork.identity.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing an active user session.
 * Sessions are tracked for security and multi-device management.
 * 
 * Performance: Stored in Redis for fast lookups (<5ms)
 */
@Value
@Builder
public class UserSession {
    UUID id;
    UUID userId;
    String deviceInfo; // User agent string
    String ipAddress; // Client IP address
    Instant createdAt; // Session creation time
    Instant lastAccessedAt; // Last activity time
    Instant expiresAt; // Session expiration

    /**
     * Check if session has expired.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Check if session is still active.
     */
    public boolean isActive() {
        return !isExpired();
    }

    /**
     * Update last accessed timestamp.
     * Returns new session with updated timestamp (immutable).
     */
    public UserSession updateLastAccess() {
        return UserSession.builder()
                .id(this.id)
                .userId(this.userId)
                .deviceInfo(this.deviceInfo)
                .ipAddress(this.ipAddress)
                .createdAt(this.createdAt)
                .lastAccessedAt(Instant.now())
                .expiresAt(this.expiresAt)
                .build();
    }
}
