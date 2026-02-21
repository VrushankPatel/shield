package com.shield.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final Key signingKey;
    private final long accessTokenTtlMinutes;
    private final long refreshTokenTtlMinutes;

    public JwtService(
            @Value("${shield.security.jwt.secret}") String secret,
            @Value("${shield.security.jwt.access-token-ttl-minutes}") long accessTokenTtlMinutes,
            @Value("${shield.security.jwt.refresh-token-ttl-minutes}") long refreshTokenTtlMinutes) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenTtlMinutes = accessTokenTtlMinutes;
        this.refreshTokenTtlMinutes = refreshTokenTtlMinutes;
    }

    public String generateAccessToken(UUID userId, UUID tenantId, String email, String role) {
        return generateToken(userId, tenantId, email, role, accessTokenTtlMinutes, "access", "USER", 0L);
    }

    public String generateRefreshToken(UUID userId, UUID tenantId, String email, String role) {
        return generateToken(userId, tenantId, email, role, refreshTokenTtlMinutes, "refresh", "USER", 0L);
    }

    public String generateRootAccessToken(UUID rootAccountId, String loginId, long tokenVersion) {
        return generateToken(rootAccountId, null, loginId, "ROOT", accessTokenTtlMinutes, "access", "ROOT", tokenVersion);
    }

    public String generateRootRefreshToken(UUID rootAccountId, String loginId, long tokenVersion) {
        return generateToken(rootAccountId, null, loginId, "ROOT", refreshTokenTtlMinutes, "refresh", "ROOT", tokenVersion);
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String stripBearerPrefix(String value) {
        if (value == null || !value.startsWith("Bearer ")) {
            return value;
        }
        return value.substring(7);
    }

    private String generateToken(
            UUID userId,
            UUID tenantId,
            String email,
            String role,
            long ttlMinutes,
            String tokenType,
            String principalType,
            long tokenVersion) {

        Instant now = Instant.now();
        Instant expiration = now.plus(ttlMinutes, ChronoUnit.MINUTES);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        if (tenantId != null) {
            claims.put("tenantId", tenantId.toString());
        }
        claims.put("role", role);
        claims.put("tokenType", tokenType);
        claims.put("principalType", principalType);
        claims.put("tokenVersion", tokenVersion);

        return Jwts.builder()
                .subject(email)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();
    }
}
