package com.eduwork.identity.infrastructure.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for extracting client information from HTTP requests.
 */
@Slf4j
public class RequestUtils {

    private RequestUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Extracts the real client IP address from the HTTP request.
     * Handles proxy and load balancer scenarios by checking various headers.
     *
     * @param request the HTTP servlet request
     * @return the client's IP address
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                log.debug("Extracted IP address from header {}: {}", header, ip);
                return ip;
            }
        }

        // Fallback to remote address
        String remoteAddr = request.getRemoteAddr();
        log.debug("Using remote address as IP: {}", remoteAddr);
        return remoteAddr;
    }

    /**
     * Gets the User-Agent header from the request.
     *
     * @param request the HTTP servlet request
     * @return the User-Agent string, or null if not present
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
