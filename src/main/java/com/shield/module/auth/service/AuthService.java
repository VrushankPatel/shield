package com.shield.module.auth.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.UnauthorizedException;
import com.shield.module.auth.dto.AuthResponse;
import com.shield.module.auth.dto.ChangePasswordRequest;
import com.shield.module.auth.dto.ForgotPasswordRequest;
import com.shield.module.auth.dto.LoginRequest;
import com.shield.module.auth.dto.RefreshRequest;
import com.shield.module.auth.dto.RegisterRequest;
import com.shield.module.auth.dto.RegisterResponse;
import com.shield.module.auth.dto.ResetPasswordRequest;
import com.shield.module.auth.entity.AuthTokenEntity;
import com.shield.module.auth.entity.AuthTokenType;
import com.shield.module.auth.repository.AuthTokenRepository;
import com.shield.module.tenant.entity.TenantEntity;
import com.shield.module.tenant.repository.TenantRepository;
import com.shield.module.unit.entity.UnitEntity;
import com.shield.module.unit.repository.UnitRepository;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.entity.UserStatus;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.jwt.JwtService;
import com.shield.security.model.ShieldPrincipal;
import io.jsonwebtoken.Claims;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UnitRepository unitRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    @Value("${shield.security.jwt.access-token-ttl-minutes}")
    private long accessTokenTtlMinutes;

    @Value("${shield.auth.password-reset-ttl-minutes:30}")
    private long passwordResetTtlMinutes;

    @Value("${shield.auth.email-verification-ttl-hours:24}")
    private long emailVerificationTtlHours;

    @Value("${shield.notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${shield.notification.email.from:no-reply@shield.local}")
    private String emailFrom;

    @Value("${shield.app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        TenantEntity tenant = tenantRepository.findById(request.tenantId())
                .filter(existing -> !existing.isDeleted())
                .orElseThrow(() -> new BadRequestException("Invalid tenant id"));

        if (request.unitId() != null) {
            UnitEntity unit = unitRepository.findByIdAndDeletedFalse(request.unitId())
                    .orElseThrow(() -> new BadRequestException("Invalid unit id"));
            if (!unit.getTenantId().equals(tenant.getId())) {
                throw new BadRequestException("Unit does not belong to the requested tenant");
            }
        }

        String normalizedEmail = request.email().toLowerCase();
        if (userRepository.existsByTenantIdAndEmailIgnoreCaseAndDeletedFalse(tenant.getId(), normalizedEmail)) {
            throw new BadRequestException("Email already exists in tenant");
        }

        UserEntity user = new UserEntity();
        user.setTenantId(tenant.getId());
        user.setUnitId(request.unitId());
        user.setName(request.name());
        user.setEmail(normalizedEmail);
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setStatus(UserStatus.INACTIVE);
        UserEntity saved = userRepository.save(user);

        String token = createToken(
                saved,
                AuthTokenType.EMAIL_VERIFICATION,
                Instant.now().plus(emailVerificationTtlHours, ChronoUnit.HOURS),
                null);
        sendVerificationEmail(saved.getEmail(), token);

        auditLogService.record(saved.getTenantId(), saved.getId(), "AUTH_REGISTER", "users", saved.getId(), null);
        return new RegisterResponse(saved.getId(), saved.getTenantId(), saved.getEmail(), true);
    }

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
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCaseAndDeletedFalse(request.email())
                .ifPresent(user -> {
                    String token = createToken(
                            user,
                            AuthTokenType.PASSWORD_RESET,
                            Instant.now().plus(passwordResetTtlMinutes, ChronoUnit.MINUTES),
                            null);
                    sendPasswordResetEmail(user.getEmail(), token);
                    auditLogService.record(
                            user.getTenantId(),
                            user.getId(),
                            "AUTH_FORGOT_PASSWORD_REQUESTED",
                            "users",
                            user.getId(),
                            null);
                });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        AuthTokenEntity token = resolveValidToken(request.token(), AuthTokenType.PASSWORD_RESET);
        UserEntity user = userRepository.findById(token.getUserId())
                .filter(existing -> !existing.isDeleted())
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        token.setConsumedAt(Instant.now());
        authTokenRepository.save(token);

        auditLogService.record(user.getTenantId(), user.getId(), "AUTH_PASSWORD_RESET", "users", user.getId(), null);
    }

    @Transactional
    public void changePassword(ShieldPrincipal principal, ChangePasswordRequest request) {
        UserEntity user = userRepository.findByIdAndDeletedFalse(principal.userId())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        auditLogService.record(user.getTenantId(), user.getId(), "AUTH_PASSWORD_CHANGED", "users", user.getId(), null);
    }

    @Transactional
    public void verifyEmail(String tokenValue) {
        AuthTokenEntity token = resolveValidToken(tokenValue, AuthTokenType.EMAIL_VERIFICATION);
        UserEntity user = userRepository.findById(token.getUserId())
                .filter(existing -> !existing.isDeleted())
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        token.setConsumedAt(Instant.now());
        authTokenRepository.save(token);

        auditLogService.record(user.getTenantId(), user.getId(), "AUTH_EMAIL_VERIFIED", "users", user.getId(), null);
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

    private String createToken(UserEntity user, AuthTokenType tokenType, Instant expiresAt, String metadata) {
        consumeOpenTokens(user.getTenantId(), user.getId(), tokenType);

        AuthTokenEntity token = new AuthTokenEntity();
        token.setTenantId(user.getTenantId());
        token.setUserId(user.getId());
        token.setTokenType(tokenType);
        token.setTokenValue(generateTokenValue());
        token.setExpiresAt(expiresAt);
        token.setMetadata(metadata);
        AuthTokenEntity saved = authTokenRepository.save(token);
        return saved.getTokenValue();
    }

    private void consumeOpenTokens(UUID tenantId, UUID userId, AuthTokenType tokenType) {
        List<AuthTokenEntity> activeTokens = authTokenRepository
                .findAllByTenantIdAndUserIdAndTokenTypeAndConsumedAtIsNullAndDeletedFalse(tenantId, userId, tokenType);
        if (activeTokens.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        activeTokens.forEach(token -> token.setConsumedAt(now));
        authTokenRepository.saveAll(activeTokens);
    }

    private AuthTokenEntity resolveValidToken(String tokenValue, AuthTokenType expectedType) {
        AuthTokenEntity token = authTokenRepository.findByTokenValueAndDeletedFalse(tokenValue)
                .orElseThrow(() -> new BadRequestException("Invalid token"));

        if (token.getTokenType() != expectedType) {
            throw new BadRequestException("Invalid token type");
        }
        if (token.getConsumedAt() != null) {
            throw new BadRequestException("Token already consumed");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Token has expired");
        }
        return token;
    }

    private void sendVerificationEmail(String email, String token) {
        String verifyUrl = sanitizeBaseUrl(appBaseUrl) + "/api/v1/auth/verify-email/" + token;
        sendEmail(
                email,
                "Verify your SHIELD account",
                "Welcome to SHIELD.\n\nVerify your email using this link:\n" + verifyUrl);
    }

    private void sendPasswordResetEmail(String email, String token) {
        String resetUrl = sanitizeBaseUrl(appBaseUrl) + "/api/v1/auth/reset-password?token=" + token;
        sendEmail(
                email,
                "Reset your SHIELD password",
                "A password reset was requested for your account.\n\n"
                        + "Use this token in reset API: " + token + "\n"
                        + "Reference reset URL: " + resetUrl + "\n\n"
                        + "If you did not request this, you can ignore this email.");
    }

    private void sendEmail(String recipient, String subject, String body) {
        if (!emailEnabled) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ignored) {
            // Intentionally non-blocking for auth flows.
        }
    }

    private String sanitizeBaseUrl(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String generateTokenValue() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }
}
