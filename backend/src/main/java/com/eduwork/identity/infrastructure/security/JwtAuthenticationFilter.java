package com.eduwork.identity.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * JWT authentication filter.
 * Extracts JWT from Authorization header, validates it, and sets authentication
 * in SecurityContext.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Extract Authorization header
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token, continue without authentication
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. Extract token
            String token = authHeader.substring(7); // Remove "Bearer " prefix

            // 3. Validate token (will throw exception if invalid)
            Claims claims = jwtService.validateAccessToken(token);

            // 4. Extract user details
            UUID userId = jwtService.extractUserId(claims);
            String email = jwtService.extractEmail(claims);
            String status = claims.get("status", String.class);

            // 5. Create authentication object
            // Using userId as principal so getName() returns the UUID
            // Email and other details available in JWT claims if needed
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId.toString(), // principal (UUID as string)
                    null, // credentials (no password needed after JWT validation)
                    List.of(new SimpleGrantedAuthority("ROLE_" + status)) // authorities
            );

            // Add request details
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 6. Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT authentication successful for user: {}", email);

        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            // Don't set authentication - will be handled by Spring Security as
            // unauthenticated
        } catch (Exception e) {
            log.error("Error processing JWT token", e);
        }

        // 7. Continue filter chain
        filterChain.doFilter(request, response);
    }
}
