package com.shield.module.auth.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.UnauthorizedException;
import com.shield.module.auth.dto.AuthResponse;
import com.shield.module.auth.dto.LoginRequest;
import com.shield.module.auth.dto.RefreshRequest;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.entity.UserStatus;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    @Value("${shield.security.jwt.access-token-ttl-minutes}")
    private long accessTokenTtlMinutes;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmailIgnoreCaseAndDeletedFalse(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (user.getStatus() != UserStatus.ACTIVE || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getTenantId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getTenantId(), user.getEmail(), user.getRole().name());

        auditLogService.record(user.getTenantId(), user.getId(), "AUTH_LOGIN", "users", user.getId(), null);
        return new AuthResponse(accessToken, refreshToken, "Bearer", accessTokenTtlMinutes * 60);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String refreshToken = jwtService.stripBearerPrefix(request.refreshToken());
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        Claims claims = jwtService.parseClaims(refreshToken);
        String tokenType = claims.get("tokenType", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new UnauthorizedException("Invalid token type");
        }

        UUID userId = UUID.fromString(claims.get("userId", String.class));
        UUID tenantId = UUID.fromString(claims.get("tenantId", String.class));
        String email = claims.getSubject();
        String role = claims.get("role", String.class);

        String accessToken = jwtService.generateAccessToken(userId, tenantId, email, role);
        return new AuthResponse(accessToken, refreshToken, "Bearer", accessTokenTtlMinutes * 60);
    }

    @Transactional
    public void logout(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return;
        }

        String token = jwtService.stripBearerPrefix(authHeader);
        if (!jwtService.isTokenValid(token)) {
            return;
        }

        Claims claims = jwtService.parseClaims(token);
        UUID userId = UUID.fromString(claims.get("userId", String.class));
        UUID tenantId = UUID.fromString(claims.get("tenantId", String.class));
        auditLogService.record(tenantId, userId, "AUTH_LOGOUT", "users", userId, null);
    }
}
