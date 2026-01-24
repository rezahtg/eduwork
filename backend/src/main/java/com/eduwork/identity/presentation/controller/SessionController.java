package com.eduwork.identity.presentation.controller;

import com.eduwork.common.dto.ApiResponse;
import com.eduwork.identity.application.usecase.RevokeSessionUseCase;
import com.eduwork.identity.application.usecase.ViewActiveSessionsUseCase;
import com.eduwork.identity.presentation.dto.UserSessionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for session management.
 * Allows users to view and manage their active sessions.
 */
@Slf4j
@RestController
@RequestMapping("/users/me/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final ViewActiveSessionsUseCase viewActiveSessionsUseCase;
    private final RevokeSessionUseCase revokeSessionUseCase;

    /**
     * Get all active sessions for current user.
     * Shows device info, IP, and last access time for each session.
     * 
     * @param userId    user ID from JWT
     * @param sessionId current session ID from JWT (optional)
     * @return list of active sessions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserSessionResponse>>> getActiveSessions(
            @AuthenticationPrincipal String userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        log.info("Fetching active sessions for user: {}", userId);

        UUID currentSessionId = sessionId != null ? UUID.fromString(sessionId) : null;

        List<UserSessionResponse> sessions = viewActiveSessionsUseCase.execute(
                UUID.fromString(userId),
                currentSessionId);

        return ResponseEntity.ok(
                ApiResponse.success(sessions,
                        String.format("Found %d active session(s)", sessions.size())));
    }

    /**
     * Revoke a specific session (force logout).
     * Cannot revoke the current session (use logout endpoint instead).
     * 
     * @param userId            user ID from JWT
     * @param sessionIdToRevoke session ID to revoke
     * @return success message
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> revokeSession(
            @AuthenticationPrincipal String userId,
            @PathVariable("sessionId") UUID sessionIdToRevoke) {

        log.info("User {} revoking session: {}", userId, sessionIdToRevoke);

        revokeSessionUseCase.execute(UUID.fromString(userId), sessionIdToRevoke);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Session revoked successfully"));
    }
}
