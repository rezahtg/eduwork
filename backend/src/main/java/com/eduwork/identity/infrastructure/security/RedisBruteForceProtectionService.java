package com.eduwork.identity.infrastructure.security;

import com.eduwork.identity.domain.service.BruteForceProtectionService;
import com.eduwork.identity.infrastructure.config.BruteForceConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based implementation of brute force protection service.
 * Uses Redis for fast, atomic operations and automatic TTL-based cleanup.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisBruteForceProtectionService implements BruteForceProtectionService {

    private final RedisTemplate<String, String> redisTemplate;
    private final BruteForceConfig config;

    private static final String ACCOUNT_KEY_PREFIX = "login:attempts:account:";
    private static final String IP_KEY_PREFIX = "login:attempts:ip:";
    private static final String LOCK_KEY_PREFIX = "login:locked:";

    @Override
    public void recordFailedAttempt(String email, String ipAddress) {
        String accountKey = ACCOUNT_KEY_PREFIX + email.toLowerCase();
        String ipKey = IP_KEY_PREFIX + ipAddress;

        // Increment counters with TTL
        Long accountAttempts = redisTemplate.opsForValue().increment(accountKey);
        redisTemplate.expire(accountKey, config.getAttemptWindow().toMillis(), TimeUnit.MILLISECONDS);

        Long ipAttempts = redisTemplate.opsForValue().increment(ipKey);
        redisTemplate.expire(ipKey, config.getIpWindow().toMillis(), TimeUnit.MILLISECONDS);

        log.warn("Failed login attempt - Email: {}, IP: {}, Account attempts: {}, IP attempts: {}",
                email, ipAddress, accountAttempts, ipAttempts);

        // Check if account should be locked
        if (accountAttempts != null && accountAttempts >= config.getMaxAttempts()) {
            lockAccount(email);
            log.warn("Account locked due to {} failed attempts: {}", accountAttempts, email);
        }
    }

    @Override
    public void recordSuccessfulLogin(String email, String ipAddress) {
        log.info("Successful login - Email: {}, IP: {}", email, ipAddress);

        // Reset attempt counters on successful login
        resetAttempts(email);
    }

    @Override
    public boolean isAccountLocked(String email) {
        String lockKey = LOCK_KEY_PREFIX + email.toLowerCase();
        Boolean hasKey = redisTemplate.hasKey(lockKey);

        if (Boolean.TRUE.equals(hasKey)) {
            log.debug("Account is locked: {}", email);
            return true;
        }

        return false;
    }

    @Override
    public boolean isIpBlocked(String ipAddress) {
        String ipKey = IP_KEY_PREFIX + ipAddress;
        String attemptsStr = redisTemplate.opsForValue().get(ipKey);

        if (attemptsStr != null) {
            int attempts = Integer.parseInt(attemptsStr);
            boolean blocked = attempts >= config.getMaxIpAttempts();

            if (blocked) {
                log.warn("IP address is blocked due to {} failed attempts: {}", attempts, ipAddress);
            }

            return blocked;
        }

        return false;
    }

    @Override
    public void resetAttempts(String email) {
        String accountKey = ACCOUNT_KEY_PREFIX + email.toLowerCase();
        Boolean deleted = redisTemplate.delete(accountKey);

        if (Boolean.TRUE.equals(deleted)) {
            log.debug("Reset failed attempts for email: {}", email);
        }
    }

    @Override
    public int getFailedAttempts(String email) {
        String accountKey = ACCOUNT_KEY_PREFIX + email.toLowerCase();
        String attemptsStr = redisTemplate.opsForValue().get(accountKey);

        if (attemptsStr != null) {
            return Integer.parseInt(attemptsStr);
        }

        return 0;
    }

    /**
     * Locks an account by setting a lock key with TTL.
     */
    private void lockAccount(String email) {
        String lockKey = LOCK_KEY_PREFIX + email.toLowerCase();

        redisTemplate.opsForValue().set(
                lockKey,
                Instant.now().toString(),
                config.getLockDuration().toMillis(),
                TimeUnit.MILLISECONDS);

        log.info("Account locked for {} minutes: {}",
                config.getLockDuration().toMinutes(), email);
    }
}
