package com.eduwork.booking.presentation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for booking a session.
 */
public record BookSessionRequest(
        @NotNull(message = "Session ID is required") UUID sessionId) {
}
