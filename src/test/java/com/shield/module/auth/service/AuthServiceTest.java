package com.shield.module.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.UnauthorizedException;
import com.shield.module.auth.dto.AuthResponse;
import com.shield.module.auth.dto.LoginRequest;
import com.shield.module.auth.dto.RegisterRequest;
import com.shield.module.auth.dto.RegisterResponse;
import com.shield.module.auth.entity.AuthTokenEntity;
import com.shield.module.auth.entity.AuthTokenType;
import com.shield.module.auth.repository.AuthTokenRepository;
import com.shield.module.tenant.entity.TenantEntity;
import com.shield.module.tenant.repository.TenantRepository;
import com.shield.module.unit.entity.UnitEntity;
import com.shield.module.auth.dto.ChangePasswordRequest;
import com.shield.module.unit.repository.UnitRepository;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.entity.UserRole;
import com.shield.module.user.entity.UserStatus;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.jwt.JwtService;
import com.shield.security.model.ShieldPrincipal;
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
    private JwtService jwtService;

    @Mock
    private AuditLogService auditLogService;

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
                jwtService,
                auditLogService);

        ReflectionTestUtils.setField(authService, "accessTokenTtlMinutes", 30L);
        ReflectionTestUtils.setField(authService, "passwordResetTtlMinutes", 30L);
        ReflectionTestUtils.setField(authService, "emailVerificationTtlHours", 24L);
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
}
