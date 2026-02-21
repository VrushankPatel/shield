package com.shield.module.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doThrow;

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
import com.shield.module.user.entity.UserRole;
import com.shield.module.user.entity.UserStatus;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.jwt.JwtService;
import com.shield.security.model.ShieldPrincipal;
import com.shield.security.policy.PasswordPolicyService;
import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HexFormat;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SmsOtpSender smsOtpSender;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private PasswordPolicyService passwordPolicyService;

    @Mock
    private TransactionStatus transactionStatus;

    @Mock
    private Claims claims;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                tenantRepository,
                unitRepository,
                authTokenRepository,
                passwordEncoder,
                mailSender,
                smsOtpSender,
                jwtService,
                auditLogService,
                transactionManager,
                passwordPolicyService);

        lenient().when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

        ReflectionTestUtils.setField(authService, "accessTokenTtlMinutes", 30L);
        ReflectionTestUtils.setField(authService, "passwordResetTtlMinutes", 30L);
        ReflectionTestUtils.setField(authService, "emailVerificationTtlHours", 24L);
        ReflectionTestUtils.setField(authService, "loginOtpTtlMinutes", 5L);
        ReflectionTestUtils.setField(authService, "loginOtpMaxAttempts", 5);
        ReflectionTestUtils.setField(authService, "userLockoutMaxFailedAttempts", 5);
        ReflectionTestUtils.setField(authService, "userLockoutDurationMinutes", 30L);
        ReflectionTestUtils.setField(authService, "emailEnabled", false);
        ReflectionTestUtils.setField(authService, "appBaseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(authService, "emailFrom", "no-reply@shield.local");
    }

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void loginShouldReturnTokensWhenCredentialsAreValid() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setEmail("admin@shield.dev");
        user.setPasswordHash("hash");
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmailIgnoreCaseAndDeletedFalse("admin@shield.dev")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);
        when(jwtService.generateAccessToken(userId, tenantId, "admin@shield.dev", "ADMIN")).thenReturn("access");
        when(jwtService.generateRefreshToken(userId, tenantId, "admin@shield.dev", "ADMIN")).thenReturn("refresh");
        when(jwtService.parseClaims("refresh")).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(Date.from(Instant.now().plusSeconds(1800)));
        when(authTokenRepository.save(any(AuthTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.login(new LoginRequest("admin@shield.dev", "password123"));

        assertEquals("access", response.accessToken());
        assertEquals("refresh", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(1800L, response.expiresIn());
        verify(auditLogService).logEvent(eq(tenantId), eq(userId), eq("AUTH_LOGIN"), eq("users"), eq(userId), any());
        verify(authTokenRepository).save(any(AuthTokenEntity.class));
    }

    @Test
    void loginShouldThrowUnauthorizedWhenPasswordDoesNotMatch() {
        UserEntity user = new UserEntity();
        user.setEmail("admin@shield.dev");
        user.setPasswordHash("hash");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmailIgnoreCaseAndDeletedFalse("admin@shield.dev")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(new LoginRequest("admin@shield.dev", "bad")));
    }

    @Test
    void sendLoginOtpShouldCreateChallengeAndDispatchSms() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setEmail("admin@shield.dev");
        user.setPhone("9999999999");
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmailIgnoreCaseAndDeletedFalse("admin@shield.dev")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(any())).thenReturn("otp-hash");
        when(authTokenRepository.findAllByTenantIdAndUserIdAndTokenTypeAndConsumedAtIsNullAndDeletedFalse(
                tenantId, userId, AuthTokenType.LOGIN_OTP)).thenReturn(List.of());
        when(authTokenRepository.save(any(AuthTokenEntity.class))).thenAnswer(invocation -> {
            AuthTokenEntity token = invocation.getArgument(0);
            token.setId(UUID.randomUUID());
            token.setTokenValue("challenge-token");
            return token;
        });

        LoginOtpSendResponse response = authService.sendLoginOtp(new LoginOtpSendRequest("admin@shield.dev"));

        assertEquals("challenge-token", response.challengeToken());
        assertEquals("******9999", response.destination());
        verify(smsOtpSender).sendLoginOtp(eq("9999999999"), any(), any());
        verify(auditLogService).logEvent(eq(tenantId), eq(userId), eq("AUTH_LOGIN_OTP_SENT"), eq("users"), eq(userId), any());
    }

    @Test
    void sendLoginOtpShouldFailWhenPhoneMissing() {
        UserEntity user = new UserEntity();
        user.setEmail("admin@shield.dev");
        user.setStatus(UserStatus.ACTIVE);
        user.setPhone(null);

        when(userRepository.findByEmailIgnoreCaseAndDeletedFalse("admin@shield.dev")).thenReturn(Optional.of(user));
        LoginOtpSendRequest request = new LoginOtpSendRequest("admin@shield.dev");
        assertThrows(BadRequestException.class, () -> authService.sendLoginOtp(request));
    }

    @Test
    void verifyLoginOtpShouldIssueTokensWhenCodeMatches() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        AuthTokenEntity token = new AuthTokenEntity();
        token.setId(UUID.randomUUID());
        token.setTenantId(tenantId);
        token.setUserId(userId);
        token.setTokenType(AuthTokenType.LOGIN_OTP);
        token.setTokenValue("challenge-token");
        token.setMetadata("otpHash=otp-hash;attempts=0;maxAttempts=5");
        token.setExpiresAt(Instant.now().plusSeconds(300));

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setEmail("admin@shield.dev");
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);

        when(authTokenRepository.findByTokenValueAndDeletedFalse("challenge-token")).thenReturn(Optional.of(token));
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "otp-hash")).thenReturn(true);
        when(jwtService.generateAccessToken(userId, tenantId, "admin@shield.dev", "ADMIN")).thenReturn("access-otp");
        when(jwtService.generateRefreshToken(userId, tenantId, "admin@shield.dev", "ADMIN")).thenReturn("refresh-otp");
        when(jwtService.parseClaims("refresh-otp")).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(Date.from(Instant.now().plusSeconds(1800)));
        when(authTokenRepository.save(any(AuthTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.verifyLoginOtp(new LoginOtpVerifyRequest("challenge-token", "123456"));

        assertEquals("access-otp", response.accessToken());
        assertEquals("refresh-otp", response.refreshToken());
        verify(auditLogService).logEvent(eq(tenantId), eq(userId), eq("AUTH_LOGIN_OTP_VERIFIED"), eq("users"), eq(userId), any());
    }

    @Test
    void verifyLoginOtpShouldConsumeTokenWhenMaxAttemptsReached() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        AuthTokenEntity token = new AuthTokenEntity();
        token.setId(UUID.randomUUID());
        token.setTenantId(tenantId);
        token.setUserId(userId);
        token.setTokenType(AuthTokenType.LOGIN_OTP);
        token.setTokenValue("challenge-token");
        token.setMetadata("otpHash=otp-hash;attempts=4;maxAttempts=5");
        token.setExpiresAt(Instant.now().plusSeconds(300));

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setStatus(UserStatus.ACTIVE);

        when(authTokenRepository.findByTokenValueAndDeletedFalse("challenge-token")).thenReturn(Optional.of(token));
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("000000", "otp-hash")).thenReturn(false);
        when(authTokenRepository.save(any(AuthTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginOtpVerifyRequest request = new LoginOtpVerifyRequest("challenge-token", "000000");
        assertThrows(UnauthorizedException.class, () -> authService.verifyLoginOtp(request));

        assertNotNull(token.getConsumedAt());
        verify(authTokenRepository).save(token);
    }

    @Test
    void registerShouldCreateInactiveUserAndVerificationToken() {
        UUID tenantId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        TenantEntity tenant = new TenantEntity();
        tenant.setId(tenantId);
        tenant.setName("Test Society");

        UnitEntity unit = new UnitEntity();
        unit.setId(unitId);
        unit.setTenantId(tenantId);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(unitRepository.findByIdAndDeletedFalse(unitId)).thenReturn(Optional.of(unit));
        when(userRepository.existsByTenantIdAndEmailIgnoreCaseAndDeletedFalse(tenantId, "resident@shield.dev"))
                .thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(authTokenRepository.findAllByTenantIdAndUserIdAndTokenTypeAndConsumedAtIsNullAndDeletedFalse(
                tenantId,
                userId,
                AuthTokenType.EMAIL_VERIFICATION)).thenReturn(List.of());

        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(userId);
            return entity;
        });

        when(authTokenRepository.save(any(AuthTokenEntity.class))).thenAnswer(invocation -> {
            AuthTokenEntity token = invocation.getArgument(0);
            token.setId(UUID.randomUUID());
            token.setTokenValue("verification-token");
            token.setExpiresAt(Instant.now().plusSeconds(3600));
            return token;
        });

        RegisterResponse response = authService.register(new RegisterRequest(
                tenantId,
                unitId,
                "Resident",
                "resident@shield.dev",
                "9999999999",
                "password123",
                UserRole.TENANT));

        assertEquals(userId, response.userId());
        assertEquals(tenantId, response.tenantId());
        assertEquals("resident@shield.dev", response.email());

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(UserStatus.INACTIVE, userCaptor.getValue().getStatus());

        verify(authTokenRepository).save(any(AuthTokenEntity.class));
        verify(auditLogService).logEvent(eq(tenantId), eq(userId), eq("AUTH_REGISTER"), eq("users"), eq(userId), any());
    }

    @Test
    void registerShouldRejectWeakPasswordBeforePersistence() {
        UUID tenantId = UUID.randomUUID();
        RegisterRequest request = new RegisterRequest(
                tenantId,
                null,
                "Resident",
                "resident@shield.dev",
                "9999999999",
                "weak",
                UserRole.TENANT);

        doThrow(new BadRequestException("Password does not meet security policy"))
                .when(passwordPolicyService)
                .validateOrThrow("weak", "Password");

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(tenantRepository, never()).findById(any());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void refreshShouldIssueNewAccessToken() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        String rawRefreshToken = "refresh-token";
        String refreshTokenHash = hashToken(rawRefreshToken);

        AuthTokenEntity refreshSession = new AuthTokenEntity();
        refreshSession.setId(UUID.randomUUID());
        refreshSession.setTenantId(tenantId);
        refreshSession.setUserId(userId);
        refreshSession.setTokenType(AuthTokenType.REFRESH_SESSION);
        refreshSession.setTokenValue(refreshTokenHash);
        refreshSession.setExpiresAt(Instant.now().plusSeconds(600));

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setEmail("admin@shield.dev");
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);

        Claims rotatedClaims = org.mockito.Mockito.mock(Claims.class);

        when(jwtService.stripBearerPrefix("Bearer refresh-token")).thenReturn(rawRefreshToken);
        when(jwtService.isTokenValid(rawRefreshToken)).thenReturn(true);
        when(jwtService.parseClaims(rawRefreshToken)).thenReturn(claims);
        when(claims.get("tokenType", String.class)).thenReturn("refresh");
        when(claims.get("principalType", String.class)).thenReturn("USER");
        when(claims.get("userId", String.class)).thenReturn(userId.toString());
        when(authTokenRepository.findByTokenTypeAndTokenValueAndConsumedAtIsNullAndDeletedFalse(
                AuthTokenType.REFRESH_SESSION, refreshTokenHash)).thenReturn(Optional.of(refreshSession));
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(userId, tenantId, "admin@shield.dev", "ADMIN")).thenReturn("new-access");
        when(jwtService.generateRefreshToken(userId, tenantId, "admin@shield.dev", "ADMIN")).thenReturn("rotated-refresh");
        when(jwtService.parseClaims("rotated-refresh")).thenReturn(rotatedClaims);
        when(rotatedClaims.getExpiration()).thenReturn(Date.from(Instant.now().plusSeconds(1800)));
        when(authTokenRepository.save(any(AuthTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.refresh(new RefreshRequest("Bearer refresh-token"));

        assertEquals("new-access", response.accessToken());
        assertEquals("rotated-refresh", response.refreshToken());
        verify(auditLogService).logEvent(eq(tenantId), eq(userId), eq("AUTH_REFRESH"), eq("users"), eq(userId), any());
    }

    @Test
    void forgotPasswordShouldCreateResetTokenForExistingUser() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setEmail("resident@shield.dev");

        when(userRepository.findByEmailIgnoreCaseAndDeletedFalse("resident@shield.dev")).thenReturn(Optional.of(user));
        when(authTokenRepository.findAllByTenantIdAndUserIdAndTokenTypeAndConsumedAtIsNullAndDeletedFalse(
                tenantId,
                userId,
                AuthTokenType.PASSWORD_RESET)).thenReturn(List.of());
        when(authTokenRepository.save(any(AuthTokenEntity.class))).thenAnswer(invocation -> {
            AuthTokenEntity token = invocation.getArgument(0);
            token.setId(UUID.randomUUID());
            token.setTokenValue("reset-token");
            return token;
        });

        authService.forgotPassword(new ForgotPasswordRequest("resident@shield.dev"));

        verify(authTokenRepository).save(any(AuthTokenEntity.class));
        verify(auditLogService).logEvent(eq(tenantId), eq(userId), eq("AUTH_FORGOT_PASSWORD_REQUESTED"), eq("users"), eq(userId), any());
    }

    @Test
    void resetPasswordShouldConsumeTokenAndUpdatePassword() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        AuthTokenEntity token = new AuthTokenEntity();
        token.setId(UUID.randomUUID());
        token.setTenantId(tenantId);
        token.setUserId(userId);
        token.setTokenType(AuthTokenType.PASSWORD_RESET);
        token.setTokenValue("reset-token");
        token.setExpiresAt(Instant.now().plusSeconds(300));

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setPasswordHash("old-hash");

        when(authTokenRepository.findByTokenValueAndDeletedFalse("reset-token")).thenReturn(Optional.of(token));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPassword#123")).thenReturn("new-hash");
        when(authTokenRepository.findAllByTenantIdAndUserIdAndTokenTypeAndConsumedAtIsNullAndDeletedFalse(
                tenantId, userId, AuthTokenType.REFRESH_SESSION)).thenReturn(List.of());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.resetPassword(new ResetPasswordRequest("reset-token", "NewPassword#123"));

        assertEquals("new-hash", user.getPasswordHash());
        verify(authTokenRepository).save(any(AuthTokenEntity.class));
        verify(auditLogService).logEvent(eq(tenantId), eq(userId), eq("AUTH_PASSWORD_RESET"), eq("users"), eq(userId), any());
    }

    @Test
    void verifyEmailShouldActivateUser() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        AuthTokenEntity token = new AuthTokenEntity();
        token.setId(UUID.randomUUID());
        token.setTenantId(tenantId);
        token.setUserId(userId);
        token.setTokenType(AuthTokenType.EMAIL_VERIFICATION);
        token.setTokenValue("verify-token");
        token.setExpiresAt(Instant.now().plusSeconds(300));

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setStatus(UserStatus.INACTIVE);

        when(authTokenRepository.findByTokenValueAndDeletedFalse("verify-token")).thenReturn(Optional.of(token));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.verifyEmail("verify-token");

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(authTokenRepository).save(any(AuthTokenEntity.class));
    }

    @Test
    void changePasswordShouldFailWhenCurrentPasswordIsInvalid() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setPasswordHash("stored-hash");

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-current", "stored-hash")).thenReturn(false);

        ShieldPrincipal principal = new ShieldPrincipal(userId, tenantId, "user@shield.dev", "TENANT");
        ChangePasswordRequest request = new ChangePasswordRequest("wrong-current", "new-pass-123");

        assertThrows(UnauthorizedException.class, () -> authService.changePassword(principal, request));
    }

    @Test
    void changePasswordShouldRevokeRefreshSessionsWhenSuccessful() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setPasswordHash("stored-hash");

        AuthTokenEntity session = new AuthTokenEntity();
        session.setId(UUID.randomUUID());
        session.setTenantId(tenantId);
        session.setUserId(userId);
        session.setTokenType(AuthTokenType.REFRESH_SESSION);
        session.setTokenValue(hashToken("refresh"));
        session.setExpiresAt(Instant.now().plusSeconds(300));

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-pass", "stored-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-pass-123", "stored-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-pass-123")).thenReturn("new-hash");
        when(authTokenRepository.findAllByTenantIdAndUserIdAndTokenTypeAndConsumedAtIsNullAndDeletedFalse(
                tenantId, userId, AuthTokenType.REFRESH_SESSION)).thenReturn(List.of(session));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(userId, tenantId, "user@shield.dev", "TENANT");
        authService.changePassword(principal, new ChangePasswordRequest("current-pass", "new-pass-123"));

        assertEquals("new-hash", user.getPasswordHash());
        verify(authTokenRepository).saveAll(any());
    }

    @Test
    void logoutShouldSkipAuditWhenTokenInvalid() {
        when(jwtService.stripBearerPrefix("Bearer bad")).thenReturn("bad");
        when(jwtService.isTokenValid("bad")).thenReturn(false);

        authService.logout("Bearer bad");

        verify(auditLogService, never()).logEvent(any(), any(), any(), any(), any(), any());
    }

    @Test
    void logoutShouldRevokeRefreshSessionsForUserPrincipal() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        AuthTokenEntity session = new AuthTokenEntity();
        session.setId(UUID.randomUUID());
        session.setTenantId(tenantId);
        session.setUserId(userId);
        session.setTokenType(AuthTokenType.REFRESH_SESSION);
        session.setTokenValue(hashToken("refresh"));
        session.setExpiresAt(Instant.now().plusSeconds(300));

        when(jwtService.stripBearerPrefix("Bearer access")).thenReturn("access");
        when(jwtService.isTokenValid("access")).thenReturn(true);
        when(jwtService.parseClaims("access")).thenReturn(claims);
        when(claims.get("principalType", String.class)).thenReturn("USER");
        when(claims.get("userId", String.class)).thenReturn(userId.toString());
        when(claims.get("tenantId", String.class)).thenReturn(tenantId.toString());
        when(authTokenRepository.findAllByTenantIdAndUserIdAndTokenTypeAndConsumedAtIsNullAndDeletedFalse(
                tenantId, userId, AuthTokenType.REFRESH_SESSION)).thenReturn(List.of(session));

        authService.logout("Bearer access");

        verify(authTokenRepository).saveAll(any());
        verify(auditLogService).logEvent(eq(tenantId), eq(userId), eq("AUTH_LOGOUT"), eq("users"), eq(userId), any());
    }

    @Test
    void loginShouldLockAccountAfterConfiguredFailedAttempts() {
        ReflectionTestUtils.setField(authService, "userLockoutMaxFailedAttempts", 2);
        ReflectionTestUtils.setField(authService, "userLockoutDurationMinutes", 5L);
        setRequestContext("203.0.113.9", "JUnit-Agent");

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setTenantId(UUID.randomUUID());
        user.setEmail("locked@shield.dev");
        user.setPasswordHash("hash");
        user.setRole(UserRole.TENANT);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmailIgnoreCaseAndDeletedFalse("locked@shield.dev")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad-pass", "hash")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(new LoginRequest("locked@shield.dev", "bad-pass")));
        assertThrows(UnauthorizedException.class, () -> authService.login(new LoginRequest("locked@shield.dev", "bad-pass")));

        assertEquals(2, user.getFailedLoginAttempts());
        assertNotNull(user.getLockedUntil());
        verify(auditLogService).logEvent(
                eq(user.getTenantId()),
                eq(user.getId()),
                eq("AUTH_LOGIN_LOCKED"),
                eq("users"),
                eq(user.getId()),
                any());
    }

    @Test
    void loginShouldRejectWhenAccountIsLockedEvenWithCorrectPassword() {
        setRequestContext("198.51.100.10", "JUnit-Agent");

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setTenantId(UUID.randomUUID());
        user.setEmail("locked-now@shield.dev");
        user.setPasswordHash("hash");
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setLockedUntil(Instant.now().plusSeconds(60));

        when(userRepository.findByEmailIgnoreCaseAndDeletedFalse("locked-now@shield.dev")).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedException.class, () -> authService.login(new LoginRequest("locked-now@shield.dev", "good-pass")));
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void loginShouldEmitSuspiciousAuditWhenIpChanges() {
        setRequestContext("192.0.2.66", "JUnit-Agent");

        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setEmail("admin@shield.dev");
        user.setPasswordHash("hash");
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setLastLoginIp("198.51.100.25");

        when(userRepository.findByEmailIgnoreCaseAndDeletedFalse("admin@shield.dev")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);
        when(jwtService.generateAccessToken(userId, tenantId, "admin@shield.dev", "ADMIN")).thenReturn("access");
        when(jwtService.generateRefreshToken(userId, tenantId, "admin@shield.dev", "ADMIN")).thenReturn("refresh");
        when(jwtService.parseClaims("refresh")).thenReturn(claims);
        when(claims.getExpiration()).thenReturn(Date.from(Instant.now().plusSeconds(1800)));
        when(authTokenRepository.save(any(AuthTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.login(new LoginRequest("admin@shield.dev", "password123"));

        verify(auditLogService).logEvent(eq(tenantId), eq(userId), eq("AUTH_LOGIN_SUSPICIOUS"), eq("users"), eq(userId), any());
    }

    private void setRequestContext(String ipAddress, String userAgent) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(ipAddress);
        request.addHeader("User-Agent", userAgent);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
