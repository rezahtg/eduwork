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
 * Use case for manually completing a session.
 * 
 * Business Rules:
 * - Only IN_PROGRESS sessions can be completed
 * - Marks all in-session bookings as COMPLETED
 * - Mentor must own the session's schedule (authorization in controller)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompleteSessionUseCase {

    private final SessionRepository sessionRepository;

    /**
     * Execute the use case to complete a session.
     * 
     * @param sessionId ID of the session to complete
     * @return Updated session response
     * @throws SessionNotFoundException   if session doesn't exist
     * @throws SessionValidationException if session cannot be completed (wrong
     *                                    status)
     */
    @Transactional
    public Session execute(UUID sessionId) {
        log.info("Completing session: {}", sessionId);

        // Load session
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // Complete session (domain logic handles validation)
        try {
            session.complete();
        } catch (IllegalStateException e) {
            throw new SessionValidationException(e.getMessage());
        }

        // Save session
        Session updatedSession = sessionRepository.save(session);

        log.info("Successfully completed session: {} (status: {})",
                sessionId, updatedSession.getStatus());

        return updatedSession;
    }
}
