package com.eduwork.identity.presentation.dto;

import com.eduwork.identity.domain.model.UserSession;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for user session information.
 */
@Value
@Builder
public class UserSessionResponse {
    UUID id;
    String deviceInfo;
    String ipAddress;
    Instant createdAt;
    Instant lastAccessedAt;
    Instant expiresAt;
    boolean isCurrentSession;

    /**
     * Factory method to create response from domain UserSession.
     * 
     * @param session          domain session
     * @param currentSessionId ID of the current session (from JWT)
     * @return session response DTO
     */
    public static UserSessionResponse from(UserSession session, UUID currentSessionId) {
        return UserSessionResponse.builder()
                .id(session.getId())
                .deviceInfo(session.getDeviceInfo())
                .ipAddress(session.getIpAddress())
                .createdAt(session.getCreatedAt())
                .lastAccessedAt(session.getLastAccessedAt())
                .expiresAt(session.getExpiresAt())
                .isCurrentSession(session.getId().equals(currentSessionId))
                .build();
    }
}
