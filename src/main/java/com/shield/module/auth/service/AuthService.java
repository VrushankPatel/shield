package com.shield.module.auth.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.UnauthorizedException;
import com.shield.module.auth.dto.AuthResponse;
import com.shield.module.auth.dto.ChangePasswordRequest;
import com.shield.module.auth.dto.ForgotPasswordRequest;
import com.shield.module.auth.dto.LoginRequest;
import com.shield.module.auth.dto.LoginOtpSendRequest;
import com.shield.module.auth.dto.LoginOtpSendResponse;
import com.shield.module.auth.dto.LoginOtpVerifyRequest;
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
import com.shield.module.notification.service.SmsOtpSender;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.entity.UserStatus;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.jwt.JwtService;
import com.shield.security.model.ShieldPrincipal;
import com.shield.security.policy.PasswordPolicyService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String ENTITY_USERS = "users";
    private static final String CLAIM_USER_ID = "userId";

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UnitRepository unitRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final SmsOtpSender smsOtpSender;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final PlatformTransactionManager transactionManager;
    private final PasswordPolicyService passwordPolicyService;

    @Value("${shield.security.jwt.access-token-ttl-minutes}")
    private long accessTokenTtlMinutes;

    @Value("${shield.auth.password-reset-ttl-minutes:30}")
    private long passwordResetTtlMinutes;

    @Value("${shield.auth.email-verification-ttl-hours:24}")
    private long emailVerificationTtlHours;

    @Value("${shield.auth.login-otp-ttl-minutes:5}")
    private long loginOtpTtlMinutes;

    @Value("${shield.auth.login-otp-max-attempts:5}")
    private int loginOtpMaxAttempts;

    @Value("${shield.auth.user-lockout.max-failed-attempts:5}")
    private int userLockoutMaxFailedAttempts;

    @Value("${shield.auth.user-lockout.duration-minutes:30}")
    private long userLockoutDurationMinutes;

    @Value("${shield.notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${shield.notification.email.from:no-reply@shield.local}")
    private String emailFrom;

    @Value("${shield.app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        passwordPolicyService.validateOrThrow(request.password(), "Password");
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

        auditLogService.logEvent(saved.getTenantId(), saved.getId(), "AUTH_REGISTER", ENTITY_USERS, saved.getId(), null);
        return new RegisterResponse(saved.getId(), saved.getTenantId(), saved.getEmail(), true);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        LoginRequestMetadata requestMetadata = resolveRequestMetadata();
        UserEntity user = userRepository.findByEmailIgnoreCaseAndDeletedFalse(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (isCurrentlyLocked(user)) {
            auditLogService.logEvent(
                    user.getTenantId(),
                    user.getId(),
                    "AUTH_LOGIN_LOCKED",
                    ENTITY_USERS,
                    user.getId(),
                    buildLoginAuditPayload(requestMetadata, Map.of(
                            "reason", "LOCKED_WINDOW",
                            "lockedUntil", String.valueOf(user.getLockedUntil()))));
            throw new UnauthorizedException("Account is temporarily locked. Try again later");
        }

        if (user.getStatus() != UserStatus.ACTIVE || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            String failureReason = user.getStatus() != UserStatus.ACTIVE ? "USER_INACTIVE" : "PASSWORD_MISMATCH";
            registerFailedLoginAttempt(user, requestMetadata, failureReason);
            throw new UnauthorizedException("Invalid credentials");
        }

        registerSuccessfulLogin(user, requestMetadata);
        AuthResponse response = issueAuthResponse(user);
        auditLogService.logEvent(user.getTenantId(), user.getId(), "AUTH_LOGIN", ENTITY_USERS, user.getId(), null);
        return response;
    }

    @Transactional
    public LoginOtpSendResponse sendLoginOtp(LoginOtpSendRequest request) {
        LoginRequestMetadata requestMetadata = resolveRequestMetadata();
        UserEntity user = userRepository.findByEmailIgnoreCaseAndDeletedFalse(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (isCurrentlyLocked(user)) {
            auditLogService.logEvent(
                    user.getTenantId(),
                    user.getId(),
                    "AUTH_LOGIN_LOCKED",
                    ENTITY_USERS,
                    user.getId(),
                    buildLoginAuditPayload(requestMetadata, Map.of(
                            "reason", "LOCKED_WINDOW",
                            "lockedUntil", String.valueOf(user.getLockedUntil()))));
            throw new UnauthorizedException("Account is temporarily locked. Try again later");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            registerFailedLoginAttempt(user, requestMetadata, "USER_INACTIVE");
            throw new UnauthorizedException("Invalid credentials");
        }

        if (user.getPhone() == null || user.getPhone().isBlank()) {
            throw new BadRequestException("Phone number is not configured for OTP login");
        }

        String otpCode = generateOtpCode();
        String metadata = encodeOtpMetadata(passwordEncoder.encode(otpCode), 0, loginOtpMaxAttempts);
        Instant expiresAt = Instant.now().plus(loginOtpTtlMinutes, ChronoUnit.MINUTES);
        String challengeToken = createToken(user, AuthTokenType.LOGIN_OTP, expiresAt, metadata);

        smsOtpSender.sendLoginOtp(user.getPhone(), otpCode, expiresAt);
        auditLogService.logEvent(user.getTenantId(), user.getId(), "AUTH_LOGIN_OTP_SENT", ENTITY_USERS, user.getId(), null);

        return new LoginOtpSendResponse(challengeToken, maskPhone(user.getPhone()), expiresAt);
    }

    @Transactional
    public AuthResponse verifyLoginOtp(LoginOtpVerifyRequest request) {
        LoginRequestMetadata requestMetadata = resolveRequestMetadata();
        AuthTokenEntity token = resolveValidToken(request.challengeToken(), AuthTokenType.LOGIN_OTP);
        UserEntity user = userRepository.findByIdAndDeletedFalse(token.getUserId())
                .orElseThrow(() -> new UnauthorizedException("Invalid OTP challenge"));

        if (isCurrentlyLocked(user)) {
            auditLogService.logEvent(
                    user.getTenantId(),
                    user.getId(),
                    "AUTH_LOGIN_LOCKED",
                    ENTITY_USERS,
                    user.getId(),
                    buildLoginAuditPayload(requestMetadata, Map.of(
                            "reason", "LOCKED_WINDOW",
                            "lockedUntil", String.valueOf(user.getLockedUntil()))));
            throw new UnauthorizedException("Account is temporarily locked. Try again later");
        }

        Map<String, String> metadata = parseOtpMetadata(token.getMetadata());
        String otpHash = metadata.get("otpHash");
        int attempts = parsePositiveInt(metadata.getOrDefault("attempts", "0"), 0);
        int maxAttempts = parsePositiveInt(metadata.getOrDefault("maxAttempts", String.valueOf(loginOtpMaxAttempts)), loginOtpMaxAttempts);

        if (otpHash == null || otpHash.isBlank()) {
            throw new BadRequestException("Invalid OTP challenge metadata");
        }

        if (!passwordEncoder.matches(request.otpCode(), otpHash)) {
            int updatedAttempts = attempts + 1;
            token.setMetadata(encodeOtpMetadata(otpHash, updatedAttempts, maxAttempts));
            if (updatedAttempts >= maxAttempts) {
                token.setConsumedAt(Instant.now());
            }
            authTokenRepository.save(token);
            registerFailedLoginAttempt(user, requestMetadata, "OTP_MISMATCH");
            throw new UnauthorizedException("Invalid OTP");
        }

        token.setConsumedAt(Instant.now());
        authTokenRepository.save(token);

        registerSuccessfulLogin(user, requestMetadata);
        AuthResponse response = issueAuthResponse(user);
        auditLogService.logEvent(user.getTenantId(), user.getId(), "AUTH_LOGIN_OTP_VERIFIED", ENTITY_USERS, user.getId(), null);
        return response;
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String refreshToken = jwtService.stripBearerPrefix(request.refreshToken());
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        Claims claims = jwtService.parseClaims(refreshToken);
        String tokenType = claims.get("tokenType", String.class);
        String principalType = claims.get("principalType", String.class);
        if (!"refresh".equals(tokenType) || !"USER".equalsIgnoreCase(principalType)) {
            throw new UnauthorizedException("Invalid token type");
        }

        UUID userId = UUID.fromString(claims.get(CLAIM_USER_ID, String.class));
        String refreshTokenHash = hashToken(refreshToken);
        AuthTokenEntity activeSession = authTokenRepository
                .findByTokenTypeAndTokenValueAndConsumedAtIsNullAndDeletedFalse(AuthTokenType.REFRESH_SESSION, refreshTokenHash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token is no longer valid"));

        if (activeSession.getExpiresAt().isBefore(Instant.now())) {
            activeSession.setConsumedAt(Instant.now());
            authTokenRepository.save(activeSession);
            throw new UnauthorizedException("Refresh token has expired");
        }

        UserEntity user = userRepository.findByIdAndDeletedFalse(userId)
                .filter(existing -> existing.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new UnauthorizedException("User not available for refresh"));

        activeSession.setConsumedAt(Instant.now());
        authTokenRepository.save(activeSession);

        AuthResponse response = issueAuthResponse(user);
        auditLogService.logEvent(user.getTenantId(), user.getId(), "AUTH_REFRESH", ENTITY_USERS, user.getId(), null);
        return response;
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
                    auditLogService.logEvent(
                            user.getTenantId(),
                            user.getId(),
                            "AUTH_FORGOT_PASSWORD_REQUESTED",
                            ENTITY_USERS,
                            user.getId(),
                            null);
                });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        passwordPolicyService.validateOrThrow(request.newPassword(), "New password");
        AuthTokenEntity token = resolveValidToken(request.token(), AuthTokenType.PASSWORD_RESET);
        UserEntity user = userRepository.findById(token.getUserId())
                .filter(existing -> !existing.isDeleted())
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        revokeRefreshSessions(user.getTenantId(), user.getId());

        token.setConsumedAt(Instant.now());
        authTokenRepository.save(token);

        auditLogService.logEvent(user.getTenantId(), user.getId(), "AUTH_PASSWORD_RESET", ENTITY_USERS, user.getId(), null);
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

        passwordPolicyService.validateOrThrow(request.newPassword(), "New password");
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        revokeRefreshSessions(user.getTenantId(), user.getId());
        auditLogService.logEvent(user.getTenantId(), user.getId(), "AUTH_PASSWORD_CHANGED", ENTITY_USERS, user.getId(), null);
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

        auditLogService.logEvent(user.getTenantId(), user.getId(), "AUTH_EMAIL_VERIFIED", ENTITY_USERS, user.getId(), null);
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
        if (!"USER".equalsIgnoreCase(claims.get("principalType", String.class))) {
            return;
        }

        UUID userId = parseUuidClaim(claims.get(CLAIM_USER_ID, String.class), CLAIM_USER_ID);
        UUID tenantId = parseUuidClaim(claims.get("tenantId", String.class), "tenantId");
        revokeRefreshSessions(tenantId, userId);
        auditLogService.logEvent(tenantId, userId, "AUTH_LOGOUT", ENTITY_USERS, userId, null);
    }

    private AuthResponse issueAuthResponse(UserEntity user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getTenantId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getTenantId(), user.getEmail(), user.getRole().name());
        createRefreshSession(user, refreshToken);
        return new AuthResponse(accessToken, refreshToken, "Bearer", accessTokenTtlMinutes * 60);
    }

    private void createRefreshSession(UserEntity user, String refreshToken) {
        Claims refreshClaims = jwtService.parseClaims(refreshToken);
        Date expiry = refreshClaims.getExpiration();
        if (expiry == null) {
            throw new UnauthorizedException("Invalid refresh token expiry");
        }

        AuthTokenEntity token = new AuthTokenEntity();
        token.setTenantId(user.getTenantId());
        token.setUserId(user.getId());
        token.setTokenType(AuthTokenType.REFRESH_SESSION);
        token.setTokenValue(hashToken(refreshToken));
        token.setExpiresAt(expiry.toInstant());
        token.setMetadata("JWT_REFRESH");
        authTokenRepository.save(token);
    }

    private void revokeRefreshSessions(UUID tenantId, UUID userId) {
        List<AuthTokenEntity> activeSessions = authTokenRepository
                .findAllByTenantIdAndUserIdAndTokenTypeAndConsumedAtIsNullAndDeletedFalse(
                        tenantId,
                        userId,
                        AuthTokenType.REFRESH_SESSION);
        if (activeSessions.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        activeSessions.forEach(token -> token.setConsumedAt(now));
        authTokenRepository.saveAll(activeSessions);
    }

    private boolean isCurrentlyLocked(UserEntity user) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now());
    }

    private void registerFailedLoginAttempt(UserEntity user, LoginRequestMetadata requestMetadata, String reason) {
        Instant now = Instant.now();
        int maxFailedAttempts = Math.max(1, userLockoutMaxFailedAttempts);
        long lockoutDurationMinutes = Math.max(1L, userLockoutDurationMinutes);
        executeInNewTransaction(() -> {
            UserEntity persistentUser = userRepository.findByIdAndDeletedFalse(user.getId()).orElse(user);
            int updatedAttempts = persistentUser.getFailedLoginAttempts() + 1;

            persistentUser.setFailedLoginAttempts(updatedAttempts);
            persistentUser.setLastFailedLoginAt(now);
            persistentUser.setLastFailedLoginIp(requestMetadata.clientIp());

            Instant lockedUntil = null;
            if (updatedAttempts >= maxFailedAttempts) {
                lockedUntil = now.plus(lockoutDurationMinutes, ChronoUnit.MINUTES);
                persistentUser.setLockedUntil(lockedUntil);
            }

            userRepository.save(persistentUser);
            Map<String, String> failurePayload = new LinkedHashMap<>();
            failurePayload.put("reason", reason);
            failurePayload.put("failedAttempts", String.valueOf(updatedAttempts));
            failurePayload.put("maxAllowedAttempts", String.valueOf(maxFailedAttempts));
            failurePayload.put("lockedUntil", lockedUntil == null ? "none" : lockedUntil.toString());
            auditLogService.logEvent(
                    persistentUser.getTenantId(),
                    persistentUser.getId(),
                    "AUTH_LOGIN_FAILED",
                    ENTITY_USERS,
                    persistentUser.getId(),
                    buildLoginAuditPayload(requestMetadata, failurePayload));

            if (lockedUntil != null) {
                auditLogService.logEvent(
                        persistentUser.getTenantId(),
                        persistentUser.getId(),
                        "AUTH_LOGIN_LOCKED",
                        ENTITY_USERS,
                        persistentUser.getId(),
                        buildLoginAuditPayload(requestMetadata, Map.of(
                                "reason", "FAILED_ATTEMPTS_EXCEEDED",
                                "failedAttempts", String.valueOf(updatedAttempts),
                                "lockedUntil", String.valueOf(lockedUntil))));
            }
        });
    }

    private void registerSuccessfulLogin(UserEntity user, LoginRequestMetadata requestMetadata) {
        String previousIp = user.getLastLoginIp();
        boolean suspiciousLogin = previousIp != null
                && !previousIp.isBlank()
                && requestMetadata.clientIp() != null
                && !requestMetadata.clientIp().isBlank()
                && !previousIp.equals(requestMetadata.clientIp());

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        user.setLastLoginIp(requestMetadata.clientIp());
        user.setLastLoginUserAgent(requestMetadata.userAgent());
        userRepository.save(user);

        if (suspiciousLogin) {
            auditLogService.logEvent(
                    user.getTenantId(),
                    user.getId(),
                    "AUTH_LOGIN_SUSPICIOUS",
                    ENTITY_USERS,
                    user.getId(),
                    buildLoginAuditPayload(requestMetadata, Map.of(
                            "reason", "IP_ADDRESS_CHANGED",
                            "previousIp", previousIp,
                            "currentIp", requestMetadata.clientIp())));
        }
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

    private String generateOtpCode() {
        int value = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(value);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return "*".repeat(Math.max(0, phone.length() - 4)) + phone.substring(phone.length() - 4);
    }

    private String encodeOtpMetadata(String otpHash, int attempts, int maxAttempts) {
        return "otpHash=" + otpHash + ";attempts=" + attempts + ";maxAttempts=" + maxAttempts;
    }

    private Map<String, String> parseOtpMetadata(String metadata) {
        Map<String, String> parsed = new HashMap<>();
        if (metadata == null || metadata.isBlank()) {
            return parsed;
        }

        String[] parts = metadata.split(";");
        for (String part : parts) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2) {
                parsed.put(pair[0], pair[1]);
            }
        }
        return parsed;
    }

    private int parsePositiveInt(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed < 0 ? fallback : parsed;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String generateTokenValue() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    private UUID parseUuidClaim(String value, String claimName) {
        try {
            return UUID.fromString(value);
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid token claim: " + claimName);
        }
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash token", ex);
        }
    }

    private LoginRequestMetadata resolveRequestMetadata() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return new LoginRequestMetadata("unknown", "unknown");
        }

        HttpServletRequest request = requestAttributes.getRequest();
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isBlank()) {
            userAgent = "unknown";
        }
        if (userAgent.length() > 512) {
            userAgent = userAgent.substring(0, 512);
        }
        return new LoginRequestMetadata(clientIp, userAgent);
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] parts = forwardedFor.split(",");
            if (parts.length > 0 && !parts[0].trim().isBlank()) {
                return parts[0].trim();
            }
        }

        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr == null || remoteAddr.isBlank()) {
            return "unknown";
        }
        return remoteAddr;
    }

    private String buildLoginAuditPayload(LoginRequestMetadata requestMetadata, Map<String, String> attributes) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("clientIp", requestMetadata.clientIp());
        payload.put("userAgent", requestMetadata.userAgent());
        payload.putAll(attributes);

        StringBuilder buffer = new StringBuilder();
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            if (buffer.length() > 0) {
                buffer.append(';');
            }
            buffer.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return buffer.toString();
    }

    private record LoginRequestMetadata(String clientIp, String userAgent) {
    }

    private void executeInNewTransaction(Runnable operation) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.executeWithoutResult(status -> operation.run());
    }
}
