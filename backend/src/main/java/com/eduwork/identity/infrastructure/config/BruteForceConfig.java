package com.eduwork.identity.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration properties for brute force protection.
 * Controls thresholds and durations for account locking and IP blocking.
 */
@Configuration
@ConfigurationProperties(prefix = "app.security.brute-force")
@Data
public class BruteForceConfig {

    /**
     * Maximum number of failed login attempts before locking the account.
     * Default: 5 attempts
     */
    private int maxAttempts = 5;

    /**
     * Time window for counting failed login attempts.
     * Attempts outside this window are not counted.
     * Default: 15 minutes
     */
    private Duration attemptWindow = Duration.ofMinutes(15);

    /**
     * Duration for which an account remains locked after exceeding max attempts.
     * Default: 30 minutes
     */
    private Duration lockDuration = Duration.ofMinutes(30);

    /**
     * Maximum number of failed login attempts from a single IP address.
     * Default: 10 attempts
     */
    private int maxIpAttempts = 10;

    /**
     * Time window for counting failed attempts from an IP address.
     * Default: 15 minutes
     */
    private Duration ipWindow = Duration.ofMinutes(15);
}
