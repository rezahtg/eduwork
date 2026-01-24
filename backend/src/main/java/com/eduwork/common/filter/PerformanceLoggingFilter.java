package com.eduwork.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Performance monitoring filter that logs request duration.
 * Automatically tracks all HTTP requests and logs slow ones.
 * 
 * Thresholds:
 * - WARN: > 500ms (p95 violation)
 * - INFO: > 100ms (p50 violation)
 * - DEBUG: All requests
 * 
 * Performance Data Format:
 * 📊 GET /api/v1/users/me/profile - 45ms (status: 200)
 * ⚠️ SLOW REQUEST: POST /api/v1/auth/login - 650ms (status: 200)
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PerformanceLoggingFilter extends OncePerRequestFilter {

    private static final long SLOW_REQUEST_THRESHOLD_MS = 500; // p95 target
    private static final long WARNING_THRESHOLD_MS = 100; // p50 target

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            logPerformance(method, uri, duration, status);
        }
    }

    private void logPerformance(String method, String uri, long duration, int status) {
        if (duration >= SLOW_REQUEST_THRESHOLD_MS) {
            // p95 violation - requires attention
            log.warn("⚠️  SLOW REQUEST: {} {} - {}ms (status: {}) - EXCEEDS p95 TARGET",
                    method, uri, duration, status);
        } else if (duration >= WARNING_THRESHOLD_MS) {
            // Between p50 and p95 - monitor
            log.info("📊 {} {} - {}ms (status: {})", method, uri, duration, status);
        } else {
            // Good performance - debug only
            log.debug("✅ {} {} - {}ms", method, uri, duration);
        }

        // TODO: Send metrics to monitoring system (Prometheus, DataDog, etc.)
        // Example: metricsService.recordRequestDuration(uri, duration);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Don't filter actuator endpoints to avoid noise
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/");
    }
}
