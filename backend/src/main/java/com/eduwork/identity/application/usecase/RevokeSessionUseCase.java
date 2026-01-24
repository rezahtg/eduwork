package com.eduwork.identity.application.usecase;

import com.eduwork.identity.application.exception.SessionNotFoundException;
import com.eduwork.identity.domain.model.UserSession;
import com.eduwork.identity.domain.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case: Revoke a specific session (force logout)
 * 
 * Security: Users can only revoke their own sessions
 * Performance: Redis delete, expected < 10ms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RevokeSessionUseCase {

    private final UserSessionRepository sessionRepository;

    /**
     * Revoke a specific session.
     * Validates that the session belongs to the user.
     * 
     * @param userId    user ID (from JWT)
     * @param sessionId session ID to revoke
     * @throws SessionNotFoundException if session not found
     * @throws SecurityException        if session doesn't belong to user
     */
    public void execute(UUID userId, UUID sessionId) {
        log.info("Revoking session {} for user: {}", sessionId, userId);

        // Verify session exists and belongs to user
        UserSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (!session.getUserId().equals(userId)) {
            log.warn("User {} attempted to revoke session {} belonging to user {}",
                    userId, sessionId, session.getUserId());
            throw new SecurityException("Cannot revoke session of another user");
        }

        // Delete session
        sessionRepository.deleteById(sessionId);
        log.info("Session {} revoked successfully", sessionId);
    }
}
