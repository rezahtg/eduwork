package com.eduwork.schedule.application.usecase;

import com.eduwork.schedule.domain.exception.SessionNotFoundException;
import com.eduwork.schedule.domain.exception.SessionValidationException;
import com.eduwork.schedule.domain.model.session.Session;
import com.eduwork.schedule.domain.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for manually starting a session.
 * 
 * Business Rules:
 * - Only CONFIRMED sessions can be started
 * - Marks all confirmed bookings as IN_SESSION
 * - Mentor must own the session's schedule (authorization in controller)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StartSessionUseCase {

    private final SessionRepository sessionRepository;

    /**
     * Execute the use case to start a session.
     * 
     * @param sessionId ID of the session to start
     * @return Updated session response
     * @throws SessionNotFoundException   if session doesn't exist
     * @throws SessionValidationException if session cannot be started (wrong
     *                                    status)
     */
    @Transactional
    public Session execute(UUID sessionId) {
        log.info("Starting session: {}", sessionId);

        // Load session
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // Start session (domain logic handles validation)
        try {
            session.start();
        } catch (IllegalStateException e) {
            throw new SessionValidationException(e.getMessage());
        }

        // Save session
        Session updatedSession = sessionRepository.save(session);

        log.info("Successfully started session: {} (status: {})",
                sessionId, updatedSession.getStatus());

        return updatedSession;
    }
}
