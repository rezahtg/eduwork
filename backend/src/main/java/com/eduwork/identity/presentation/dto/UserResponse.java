package com.eduwork.identity.presentation.dto;

import com.eduwork.identity.domain.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * REST API response DTO for user data.
 * Excludes sensitive information like password hash.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private String phone;
    private UserStatus status;
    private boolean profileComplete;
    private Instant createdAt;
}
