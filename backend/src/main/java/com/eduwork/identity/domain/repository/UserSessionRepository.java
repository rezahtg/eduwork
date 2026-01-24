package com.eduwork.identity.domain.repository;

import com.eduwork.identity.domain.model.UserSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserSession management.
 * Implementation uses Redis for fast session lookups.
 */
public interface UserSessionRepository {

    /**
     * Save or update a session.
     * 
     * @param session session to save
     * @return saved session
     */
    UserSession save(UserSession session);

    /**
     * Find session by ID.
     * 
     * @param sessionId session ID
     * @return session if found
     */
    Optional<UserSession> findById(UUID sessionId);

    /**
     * Find all active sessions for a user.
     * Only returns non-expired sessions.
     * 
     * @param userId user ID
     * @return list of active sessions
     */
    List<UserSession> findActiveByUserId(UUID userId);

    /**
     * Delete a specific session.
     * Used for logout and session revocation.
     * 
     * @param sessionId session ID to delete
     */
    void deleteById(UUID sessionId);

    /**
     * Delete all sessions for a user.
     * Used for "logout from all devices".
     * 
     * @param userId user ID
     */
    void deleteAllByUserId(UUID userId);
}
