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
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.entity.UserRole;
import com.shield.module.user.entity.UserStatus;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.jwt.JwtService;
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
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuditLogService auditLogService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService, auditLogService);
        ReflectionTestUtils.setField(authService, "accessTokenTtlMinutes", 30L);
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
}
