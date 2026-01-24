package com.eduwork.identity.application.dto;

import com.eduwork.identity.domain.model.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for user response.
 * Used for API responses to avoid exposing domain entities.
 */
@Data
@Builder
public class UserResponseDTO {

    private UUID id;
    private String email;
    private String phone;
    private UserStatus status;
    private boolean profileComplete;
    private Instant createdAt;

    // Note: passwordHash intentionally excluded for security
}
