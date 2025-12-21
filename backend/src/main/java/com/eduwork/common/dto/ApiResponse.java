package com.eduwork.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standardized API response wrapper for all endpoints.
 * Ensures consistent response structure across the entire API.
 * 
 * Success response example:
 * {
 * "success": true,
 * "timestamp": "2025-12-21T08:00:00Z",
 * "data": { ... },
 * "message": "User registered successfully"
 * }
 * 
 * Error response example:
 * {
 * "success": false,
 * "timestamp": "2025-12-21T08:00:00Z",
 * "error": {
 * "code": "EMAIL_ALREADY_EXISTS",
 * "message": "Email already registered",
 * "details": [...]
 * },
 * "path": "/api/v1/auth/register"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private Instant timestamp;
    private T data;
    private String message;
    private ApiError error;
    private String path;

    /**
     * Creates a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .timestamp(Instant.now())
                .data(data)
                .build();
    }

    /**
     * Creates a successful response with data and message.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .timestamp(Instant.now())
                .data(data)
                .message(message)
                .build();
    }

    /**
     * Creates an error response.
     */
    public static <T> ApiResponse<T> error(ApiError error, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .timestamp(Instant.now())
                .error(error)
                .path(path)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiError {
        private String code;
        private String message;
        private Object details;
    }
}
