package com.shield.module.platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.UnauthorizedException;
import com.shield.module.platform.dto.RootAuthResponse;
import com.shield.module.platform.dto.RootChangePasswordRequest;
import com.shield.module.platform.dto.RootLoginRequest;
import com.shield.module.platform.dto.RootRefreshRequest;
import com.shield.module.platform.dto.SocietyOnboardingRequest;
import com.shield.module.platform.entity.PlatformRootAccountEntity;
import com.shield.module.platform.repository.PlatformRootAccountRepository;
import com.shield.module.platform.verification.RootContactVerificationService;
import com.shield.module.tenant.repository.TenantRepository;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.jwt.JwtService;
import com.shield.security.model.ShieldPrincipal;
import io.jsonwebtoken.Claims;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PlatformRootServiceTest {

    @Mock
    private PlatformRootAccountRepository platformRootAccountRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private RootContactVerificationService rootContactVerificationService;

    @Mock
    private Claims claims;

    private PlatformRootService platformRootService;

    @BeforeEach
    void setUp() {
        platformRootService = new PlatformRootService(
                platformRootAccountRepository,
                tenantRepository,
                userRepository,
                passwordEncoder,
                jwtService,
                auditLogService,
                rootContactVerificationService);

        ReflectionTestUtils.setField(platformRootService, "accessTokenTtlMinutes", 30L);
        ReflectionTestUtils.setField(platformRootService, "maxFailedLoginAttempts", 5);
        ReflectionTestUtils.setField(platformRootService, "lockoutDurationMinutes", 30L);
    }

    @Test
    void ensureRootAccountAndGeneratePasswordIfMissingShouldCreatePassword() {
        UUID rootId = UUID.randomUUID();
        PlatformRootAccountEntity rootAccount = new PlatformRootAccountEntity();
        rootAccount.setId(rootId);
        rootAccount.setLoginId("root");
        rootAccount.setTokenVersion(0L);
        rootAccount.setActive(true);

        when(platformRootAccountRepository.findByLoginIdAndDeletedFalse("root")).thenReturn(Optional.of(rootAccount));
        when(passwordEncoder.encode(any())).thenReturn("encoded-root-password");
        when(platformRootAccountRepository.save(any(PlatformRootAccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String generatedPassword = platformRootService.ensureRootAccountAndGeneratePasswordIfMissing().orElseThrow();

        assertEquals(32, generatedPassword.length());
        assertEquals("encoded-root-password", rootAccount.getPasswordHash());
        assertTrue(rootAccount.isPasswordChangeRequired());
        verify(auditLogService).record(eq(null), eq(rootId), eq("ROOT_PASSWORD_GENERATED"), eq("platform_root_account"), eq(rootId), eq(null));
    }

    @Test
    void loginShouldIssueRootTokens() {
        UUID rootId = UUID.randomUUID();

        PlatformRootAccountEntity rootAccount = new PlatformRootAccountEntity();
        rootAccount.setId(rootId);
        rootAccount.setLoginId("root");
        rootAccount.setPasswordHash("stored-hash");
        rootAccount.setTokenVersion(2L);
        rootAccount.setPasswordChangeRequired(true);
        rootAccount.setActive(true);

        when(platformRootAccountRepository.findByLoginIdAndDeletedFalse("root")).thenReturn(Optional.of(rootAccount));
        when(passwordEncoder.matches("RootPassword#123", "stored-hash")).thenReturn(true);
        when(jwtService.generateRootAccessToken(rootId, "root", 2L)).thenReturn("root-access");
        when(jwtService.generateRootRefreshToken(rootId, "root", 2L)).thenReturn("root-refresh");

        RootAuthResponse response = platformRootService.login(new RootLoginRequest("root", "RootPassword#123"));

        assertEquals("root-access", response.accessToken());
        assertEquals("root-refresh", response.refreshToken());
        assertTrue(response.passwordChangeRequired());
        assertEquals(1800L, response.expiresIn());
    }

    @Test
    void loginShouldLockAccountAfterConfiguredFailedAttempts() {
        UUID rootId = UUID.randomUUID();

        PlatformRootAccountEntity rootAccount = new PlatformRootAccountEntity();
        rootAccount.setId(rootId);
        rootAccount.setLoginId("root");
        rootAccount.setPasswordHash("stored-hash");
        rootAccount.setFailedLoginAttempts(4);
        rootAccount.setActive(true);

        when(platformRootAccountRepository.findByLoginIdAndDeletedFalse("root")).thenReturn(Optional.of(rootAccount));
        when(passwordEncoder.matches("WrongPassword#123", "stored-hash")).thenReturn(false);

        assertThrows(
                UnauthorizedException.class,
                () -> platformRootService.login(new RootLoginRequest("root", "WrongPassword#123")));

        assertEquals(0, rootAccount.getFailedLoginAttempts());
        assertTrue(rootAccount.getLockedUntil().isAfter(Instant.now()));
        verify(auditLogService).record(
                eq(null),
                eq(rootId),
                eq("ROOT_LOGIN_FAILED"),
                eq("platform_root_account"),
                eq(rootId),
                contains("lockoutMinutes=30"));
    }

    @Test
    void loginShouldRejectWhenRootIsLocked() {
        UUID rootId = UUID.randomUUID();

        PlatformRootAccountEntity rootAccount = new PlatformRootAccountEntity();
        rootAccount.setId(rootId);
        rootAccount.setLoginId("root");
        rootAccount.setPasswordHash("stored-hash");
        rootAccount.setLockedUntil(Instant.now().plusSeconds(300));
        rootAccount.setActive(true);

        when(platformRootAccountRepository.findByLoginIdAndDeletedFalse("root")).thenReturn(Optional.of(rootAccount));

        assertThrows(
                UnauthorizedException.class,
                () -> platformRootService.login(new RootLoginRequest("root", "RootPassword#123")));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(auditLogService).record(
                eq(null),
                eq(rootId),
                eq("ROOT_LOGIN_BLOCKED"),
                eq("platform_root_account"),
                eq(rootId),
                contains("lockout"));
    }

    @Test
    void refreshShouldRotateTokensWhenClaimsAreValid() {
        UUID rootId = UUID.randomUUID();

        PlatformRootAccountEntity rootAccount = new PlatformRootAccountEntity();
        rootAccount.setId(rootId);
        rootAccount.setLoginId("root");
        rootAccount.setPasswordHash("stored-hash");
        rootAccount.setTokenVersion(5L);
        rootAccount.setPasswordChangeRequired(false);
        rootAccount.setActive(true);

        when(jwtService.stripBearerPrefix("Bearer refresh-token")).thenReturn("refresh-token");
        when(jwtService.isTokenValid("refresh-token")).thenReturn(true);
        when(jwtService.parseClaims("refresh-token")).thenReturn(claims);
        when(claims.get("tokenType", String.class)).thenReturn("refresh");
        when(claims.get("principalType", String.class)).thenReturn("ROOT");
        when(claims.get("userId", String.class)).thenReturn(rootId.toString());
        when(claims.get("tokenVersion")).thenReturn("5");

        when(platformRootAccountRepository.findByIdAndDeletedFalse(rootId)).thenReturn(Optional.of(rootAccount));
        when(jwtService.generateRootAccessToken(rootId, "root", 5L)).thenReturn("new-root-access");
        when(jwtService.generateRootRefreshToken(rootId, "root", 5L)).thenReturn("new-root-refresh");

        RootAuthResponse response = platformRootService.refresh(new RootRefreshRequest("Bearer refresh-token"));

        assertEquals("new-root-access", response.accessToken());
        assertEquals("new-root-refresh", response.refreshToken());
        assertFalse(response.passwordChangeRequired());
    }

    @Test
    void changePasswordShouldUpdateContactAndInvalidateOldTokens() {
        UUID rootId = UUID.randomUUID();

        PlatformRootAccountEntity rootAccount = new PlatformRootAccountEntity();
        rootAccount.setId(rootId);
        rootAccount.setLoginId("root");
        rootAccount.setPasswordHash("old-hash");
        rootAccount.setTokenVersion(9L);
        rootAccount.setPasswordChangeRequired(true);
        rootAccount.setActive(true);

        when(platformRootAccountRepository.findByIdAndDeletedFalse(rootId)).thenReturn(Optional.of(rootAccount));
        when(passwordEncoder.matches("NewRootPassword#123", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("NewRootPassword#123")).thenReturn("new-hash");
        when(platformRootAccountRepository.save(any(PlatformRootAccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rootContactVerificationService.verifyEmailOwnership(anyString())).thenReturn(true);
        when(rootContactVerificationService.verifyMobileOwnership(anyString())).thenReturn(true);
        when(rootContactVerificationService.emailProvider()).thenReturn("DUMMY");
        when(rootContactVerificationService.mobileProvider()).thenReturn("DUMMY");

        ShieldPrincipal principal = new ShieldPrincipal(rootId, null, "root", "ROOT", "ROOT", 9L);

        platformRootService.changePassword(
                principal,
                new RootChangePasswordRequest(
                        "root@shield.dev",
                        "9999999999",
                        "NewRootPassword#123",
                        "NewRootPassword#123"));

        assertEquals("root@shield.dev", rootAccount.getEmail());
        assertEquals("9999999999", rootAccount.getMobile());
        assertEquals("new-hash", rootAccount.getPasswordHash());
        assertFalse(rootAccount.isPasswordChangeRequired());
        assertEquals(10L, rootAccount.getTokenVersion());
        verify(rootContactVerificationService).verifyEmailOwnership("root@shield.dev");
        verify(rootContactVerificationService).verifyMobileOwnership("9999999999");
    }

    @Test
    void createSocietyWithAdminShouldFailUntilRootPasswordIsChanged() {
        UUID rootId = UUID.randomUUID();

        PlatformRootAccountEntity rootAccount = new PlatformRootAccountEntity();
        rootAccount.setId(rootId);
        rootAccount.setLoginId("root");
        rootAccount.setTokenVersion(1L);
        rootAccount.setPasswordChangeRequired(true);
        rootAccount.setActive(true);

        when(platformRootAccountRepository.findByIdAndDeletedFalse(rootId)).thenReturn(Optional.of(rootAccount));

        ShieldPrincipal principal = new ShieldPrincipal(rootId, null, "root", "ROOT", "ROOT", 1L);

        assertThrows(
                BadRequestException.class,
                () -> platformRootService.createSocietyWithAdmin(
                        principal,
                        new SocietyOnboardingRequest(
                                "Sunshine Residency",
                                "Ahmedabad",
                                "Society Admin",
                                "admin@sunshine.dev",
                                "9999999998",
                                "AdminStrong#123")));
    }

    @Test
    void isRootTokenVersionValidShouldReturnExpectedResult() {
        UUID rootId = UUID.randomUUID();
        PlatformRootAccountEntity rootAccount = new PlatformRootAccountEntity();
        rootAccount.setId(rootId);
        rootAccount.setTokenVersion(11L);
        rootAccount.setActive(true);

        when(platformRootAccountRepository.findByIdAndDeletedFalse(rootId)).thenReturn(Optional.of(rootAccount));

        assertTrue(platformRootService.isRootTokenVersionValid(rootId, 11L));
        assertFalse(platformRootService.isRootTokenVersionValid(rootId, 12L));
    }
}
