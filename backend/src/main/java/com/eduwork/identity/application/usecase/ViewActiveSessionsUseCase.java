package com.eduwork.identity.application.usecase;

import com.eduwork.identity.domain.model.UserSession;
import com.eduwork.identity.domain.repository.UserSessionRepository;
import com.eduwork.identity.presentation.dto.UserSessionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case: View all active sessions for current user
 * 
 * Performance: Redis lookups, expected < 30ms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ViewActiveSessionsUseCase {

    private final UserSessionRepository sessionRepository;

    /**
     * Get all active sessions for a user.
     * 
     * @param userId           user ID
     * @param currentSessionId current session ID (from JWT)
     * @return list of active sessions
     */
    public List<UserSessionResponse> execute(UUID userId, UUID currentSessionId) {
        log.debug("Fetching active sessions for user: {}", userId);

        List<UserSession> sessions = sessionRepository.findActiveByUserId(userId);

        log.debug("Found {} active sessions for user: {}", sessions.size(), userId);

        return sessions.stream()
                .map(session -> UserSessionResponse.from(session, currentSessionId))
                .collect(Collectors.toList());
    }
}
