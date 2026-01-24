package com.eduwork.identity.infrastructure.persistence;

import com.eduwork.identity.domain.model.UserSession;
import com.eduwork.identity.domain.repository.UserSessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis-based implementation of UserSessionRepository.
 * 
 * Storage Strategy:
 * - Key pattern: "session:{sessionId}" for session data
 * - Key pattern: "user:sessions:{userId}" for user's session IDs (Set)
 * - TTL: Matches session expiration for auto-cleanup
 * 
 * Performance: O(1) lookups, < 5ms typically
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisUserSessionRepository implements UserSessionRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String SESSION_PREFIX = "session:";
    private static final String USER_SESSIONS_PREFIX = "user:sessions:";

    @Override
    public UserSession save(UserSession session) {
        try {
            String sessionKey = SESSION_PREFIX + session.getId();
            String userSessionsKey = USER_SESSIONS_PREFIX + session.getUserId();

            // Serialize session to JSON
            String sessionJson = objectMapper.writeValueAsString(session);

            // Calculate TTL from expiration
            long ttlSeconds = Duration.between(
                    session.getCreatedAt(),
                    session.getExpiresAt()).getSeconds();

            // Save session data with TTL
            redisTemplate.opsForValue().set(sessionKey, sessionJson, ttlSeconds, TimeUnit.SECONDS);

            // Add session ID to user's session set
            redisTemplate.opsForSet().add(userSessionsKey, session.getId().toString());
            redisTemplate.expire(userSessionsKey, ttlSeconds, TimeUnit.SECONDS);

            log.debug("Saved session {} for user {}", session.getId(), session.getUserId());
            return session;

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize session", e);
            throw new RuntimeException("Failed to save session", e);
        }
    }

    @Override
    public Optional<UserSession> findById(UUID sessionId) {
        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            String sessionJson = redisTemplate.opsForValue().get(sessionKey);

            if (sessionJson == null) {
                return Optional.empty();
            }

            UserSession session = objectMapper.readValue(sessionJson, UserSession.class);

            // Double-check expiration
            if (session.isExpired()) {
                deleteById(sessionId);
                return Optional.empty();
            }

            return Optional.of(session);

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize session {}", sessionId, e);
            return Optional.empty();
        }
    }

    @Override
    public List<UserSession> findActiveByUserId(UUID userId) {
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;
        Set<String> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);

        if (sessionIds == null || sessionIds.isEmpty()) {
            return List.of();
        }

        return sessionIds.stream()
                .map(UUID::fromString)
                .map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(UserSession::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID sessionId) {
        String sessionKey = SESSION_PREFIX + sessionId;

        // Get session to find user ID
        Optional<UserSession> session = findById(sessionId);
        session.ifPresent(s -> {
            String userSessionsKey = USER_SESSIONS_PREFIX + s.getUserId();
            redisTemplate.opsForSet().remove(userSessionsKey, sessionId.toString());
        });

        redisTemplate.delete(sessionKey);
        log.debug("Deleted session {}", sessionId);
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;
        Set<String> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);

        if (sessionIds != null && !sessionIds.isEmpty()) {
            // Delete all session data
            sessionIds.forEach(id -> {
                String sessionKey = SESSION_PREFIX + id;
                redisTemplate.delete(sessionKey);
            });

            // Clear user's session set
            redisTemplate.delete(userSessionsKey);
            log.debug("Deleted all sessions for user {}", userId);
        }
    }
}
