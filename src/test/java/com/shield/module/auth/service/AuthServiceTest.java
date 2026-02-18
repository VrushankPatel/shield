package com.shield.module.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import io.jsonwebtoken.Claims;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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
                auditLogService);

        ReflectionTestUtils.setField(authService, "accessTokenTtlMinutes", 30L);
        ReflectionTestUtils.setField(authService, "passwordResetTtlMinutes", 30L);
        ReflectionTestUtils.setField(authService, "emailVerificationTtlHours", 24L);
        ReflectionTestUtils.setField(authService, "loginOtpTtlMinutes", 5L);
        ReflectionTestUtils.setField(authService, "loginOtpMaxAttempts", 5);
        ReflectionTestUtils.setField(authService, "emailEnabled", false);
        ReflectionTestUtils.setField(authService, "appBaseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(authService, "emailFrom", "no-reply@shield.local");
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

        AuthResponse response = authService.login(new LoginRequest("admin@shield.dev", "password123"));

        assertEquals("access", response.accessToken());
        assertEquals("refresh", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(1800L, response.expiresIn());
        verify(auditLogService).record(eq(tenantId), eq(userId), eq("AUTH_LOGIN"), eq("users"), eq(userId), any());
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
        verify(auditLogService).record(eq(tenantId), eq(userId), eq("AUTH_LOGIN_OTP_SENT"), eq("users"), eq(userId), any());
    }

    @Test
    void sendLoginOtpShouldFailWhenPhoneMissing() {
        UserEntity user = new UserEntity();
        user.setEmail("admin@shield.dev");
        user.setStatus(UserStatus.ACTIVE);
        user.setPhone(null);

        when(userRepository.findByEmailIgnoreCaseAndDeletedFalse("admin@shield.dev")).thenReturn(Optional.of(user));
        assertThrows(BadRequestException.class, () -> authService.sendLoginOtp(new LoginOtpSendRequest("admin@shield.dev")));
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

        AuthResponse response = authService.verifyLoginOtp(new LoginOtpVerifyRequest("challenge-token", "123456"));

        assertEquals("access-otp", response.accessToken());
        assertEquals("refresh-otp", response.refreshToken());
        verify(auditLogService).record(eq(tenantId), eq(userId), eq("AUTH_LOGIN_OTP_VERIFIED"), eq("users"), eq(userId), any());
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

        assertThrows(UnauthorizedException.class, () -> authService.verifyLoginOtp(new LoginOtpVerifyRequest("challenge-token", "000000")));

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
        verify(auditLogService).record(eq(tenantId), eq(userId), eq("AUTH_REGISTER"), eq("users"), eq(userId), any());
    }

    @Test
    void refreshShouldIssueNewAccessToken() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        when(jwtService.stripBearerPrefix("Bearer refresh-token")).thenReturn("refresh-token");
        when(jwtService.isTokenValid("refresh-token")).thenReturn(true);
        when(jwtService.parseClaims("refresh-token")).thenReturn(claims);
        when(claims.get("tokenType", String.class)).thenReturn("refresh");
        when(claims.get("userId", String.class)).thenReturn(userId.toString());
        when(claims.get("tenantId", String.class)).thenReturn(tenantId.toString());
        when(claims.getSubject()).thenReturn("admin@shield.dev");
        when(claims.get("role", String.class)).thenReturn("ADMIN");
        when(jwtService.generateAccessToken(userId, tenantId, "admin@shield.dev", "ADMIN")).thenReturn("new-access");

        AuthResponse response = authService.refresh(new RefreshRequest("Bearer refresh-token"));

        assertEquals("new-access", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
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
        verify(auditLogService).record(eq(tenantId), eq(userId), eq("AUTH_FORGOT_PASSWORD_REQUESTED"), eq("users"), eq(userId), any());
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
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.resetPassword(new ResetPasswordRequest("reset-token", "NewPassword#123"));

        assertEquals("new-hash", user.getPasswordHash());
        verify(authTokenRepository).save(any(AuthTokenEntity.class));
        verify(auditLogService).record(eq(tenantId), eq(userId), eq("AUTH_PASSWORD_RESET"), eq("users"), eq(userId), any());
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

        assertThrows(UnauthorizedException.class,
                () -> authService.changePassword(principal, new ChangePasswordRequest("wrong-current", "new-pass-123")));
    }

    @Test
    void logoutShouldSkipAuditWhenTokenInvalid() {
        when(jwtService.stripBearerPrefix("Bearer bad")).thenReturn("bad");
        when(jwtService.isTokenValid("bad")).thenReturn(false);

        authService.logout("Bearer bad");

        verify(auditLogService, never()).record(any(), any(), any(), any(), any(), any());
    }
}
