package com.eduwork.identity.infrastructure.security;

import com.eduwork.identity.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Service for JWT token generation and validation.
 * Uses JJWT library for token operations.
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final String issuer;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${app.jwt.refresh-token-expiration}") long refreshTokenExpiration,
            @Value("${app.jwt.issuer}") String issuer) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.issuer = issuer;
    }

    /**
     * Generates an access token for the user.
     * Token contains: user ID, email, role, type
     * Expires in 15 minutes.
     *
     * @param user the user
     * @return JWT access token string
     */
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenExpiration);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("status", user.getStatus().name())
                .claim("type", "ACCESS")
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Generates a refresh token JWT string.
     * Token contains: user ID, JTI (token ID), type
     * Expires in 7 days.
     *
     * @param userId  the user ID
     * @param tokenId the unique token ID (JTI)
     * @return JWT refresh token string
     */
    public String generateRefreshTokenJwt(UUID userId, String tokenId) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshTokenExpiration);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("jti", tokenId)
                .claim("type", "REFRESH")
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validates and parses an access token.
     *
     * @param token the JWT token string
     * @return parsed claims
     * @throws io.jsonwebtoken.JwtException if token is invalid or expired
     */
    public Claims validateAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Verify it's an access token
        String type = claims.get("type", String.class);
        if (!"ACCESS".equals(type)) {
            throw new IllegalArgumentException("Invalid token type");
        }

        return claims;
    }

    /**
     * Validates and parses a refresh token.
     *
     * @param token the JWT token string
     * @return parsed claims
     * @throws io.jsonwebtoken.JwtException if token is invalid or expired
     */
    public Claims validateRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Verify it's a refresh token
        String type = claims.get("type", String.class);
        if (!"REFRESH".equals(type)) {
            throw new IllegalArgumentException("Invalid token type");
        }

        return claims;
    }

    /**
     * Extracts user ID from token claims.
     *
     * @param claims the JWT claims
     * @return user ID
     */
    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Extracts email from token claims.
     *
     * @param claims the JWT claims
     * @return user email
     */
    public String extractEmail(Claims claims) {
        return claims.get("email", String.class);
    }

    /**
     * Extracts JTI (JWT ID) from refresh token claims.
     *
     * @param claims the JWT claims
     * @return token ID
     */
    public String extractJti(Claims claims) {
        return claims.get("jti", String.class);
    }

    /**
     * Gets access token expiration in seconds.
     *
     * @return expiration in seconds
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration / 1000;
    }
}
