package com.shield.module.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.user.dto.UserCreateRequest;
import com.shield.module.user.dto.UserResponse;
import com.shield.module.user.dto.UserUpdateRequest;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.entity.UserRole;
import com.shield.module.user.entity.UserStatus;
import com.shield.module.user.mapper.UserMapper;
import com.shield.module.user.repository.UserRepository;
import com.shield.tenant.context.TenantContext;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userMapper, passwordEncoder, auditLogService);
    }

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void createShouldNormalizeEmailAndEncodePassword() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        UserCreateRequest request = new UserCreateRequest(
                UUID.randomUUID(),
                "Resident",
                "Resident@Shield.Dev",
                "9999999999",
                "Password#123",
                UserRole.TENANT);

        when(userRepository.existsByTenantIdAndEmailIgnoreCaseAndDeletedFalse(tenantId, request.email())).thenReturn(false);
        when(passwordEncoder.encode("Password#123")).thenReturn("encoded-password");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(userId);
            return entity;
        });

        UserResponse mapped = new UserResponse(
                userId,
                tenantId,
                request.unitId(),
                request.name(),
                "resident@shield.dev",
                request.phone(),
                request.role(),
                UserStatus.ACTIVE,
                Instant.now(),
                Instant.now());
        when(userMapper.toResponse(any(UserEntity.class))).thenReturn(mapped);

        UserResponse response = userService.create(request);

        assertEquals("resident@shield.dev", response.email());
        verify(auditLogService).record(eq(tenantId), eq(userId), eq("USER_CREATED"), eq("users"), eq(userId), any());
    }

    @Test
    void createShouldFailWhenEmailExists() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        UserCreateRequest request = new UserCreateRequest(
                UUID.randomUUID(),
                "Resident",
                "resident@shield.dev",
                "9999999999",
                "Password#123",
                UserRole.TENANT);

        when(userRepository.existsByTenantIdAndEmailIgnoreCaseAndDeletedFalse(tenantId, request.email())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.create(request));
    }

    @Test
    void updateShouldThrowWhenUserMissing() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.update(userId, new UserUpdateRequest(
                UUID.randomUUID(),
                "Name",
                "name@shield.dev",
                "9999999999",
                UserRole.OWNER,
                UserStatus.ACTIVE)));
    }
}
