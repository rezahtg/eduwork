package com.eduwork.schedule.application.usecase;

import com.eduwork.schedule.domain.model.session.Session;
import com.eduwork.schedule.domain.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for getting session details.
 * Used to view full information about a specific session before booking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetSessionDetailsUseCase {

    private final SessionRepository sessionRepository;

    /**
     * Get details of a specific session.
     * 
     * @param sessionId Session ID
     * @return Session details
     * @throws IllegalArgumentException if session not found
     */
    @Transactional(readOnly = true)
    public Session execute(UUID sessionId) {
        log.debug("Getting details for session {}", sessionId);

        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }
}
