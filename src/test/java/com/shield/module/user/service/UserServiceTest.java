package com.shield.module.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.user.dto.UserBulkImportRequest;
import com.shield.module.user.dto.UserBulkImportResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
        verify(auditLogService).logEvent(eq(tenantId), eq(userId), eq("USER_CREATED"), eq("users"), eq(userId), any());
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

    @Test
    void listByUnitShouldReturnPagedUsers() {
        UUID unitId = UUID.randomUUID();
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setTenantId(UUID.randomUUID());
        entity.setUnitId(unitId);
        entity.setName("Unit User");

        when(userRepository.findAllByUnitIdAndDeletedFalse(eq(unitId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(userMapper.toResponse(entity)).thenReturn(new UserResponse(
                entity.getId(),
                entity.getTenantId(),
                unitId,
                entity.getName(),
                "unit@shield.dev",
                "9999999999",
                UserRole.TENANT,
                UserStatus.ACTIVE,
                Instant.now(),
                Instant.now()));

        assertEquals(1, userService.listByUnit(unitId, Pageable.ofSize(10)).content().size());
    }

    @Test
    void listByRoleShouldReturnPagedUsers() {
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setTenantId(UUID.randomUUID());
        entity.setRole(UserRole.SECURITY);

        when(userRepository.findAllByRoleAndDeletedFalse(eq(UserRole.SECURITY), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(userMapper.toResponse(entity)).thenReturn(new UserResponse(
                entity.getId(),
                entity.getTenantId(),
                null,
                "Security",
                "security@shield.dev",
                "9999999999",
                UserRole.SECURITY,
                UserStatus.ACTIVE,
                Instant.now(),
                Instant.now()));

        assertEquals(1, userService.listByRole(UserRole.SECURITY, Pageable.ofSize(10)).content().size());
    }

    @Test
    void bulkImportShouldContinueAfterRowError() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        UserCreateRequest valid = new UserCreateRequest(
                UUID.randomUUID(),
                "Valid User",
                "valid@shield.dev",
                "9999999999",
                "Password#123",
                UserRole.TENANT);
        UserCreateRequest duplicate = new UserCreateRequest(
                UUID.randomUUID(),
                "Duplicate User",
                "duplicate@shield.dev",
                "9999999998",
                "Password#123",
                UserRole.TENANT);

        when(userRepository.existsByTenantIdAndEmailIgnoreCaseAndDeletedFalse(tenantId, valid.email())).thenReturn(false);
        when(userRepository.existsByTenantIdAndEmailIgnoreCaseAndDeletedFalse(tenantId, duplicate.email())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserBulkImportResponse response = userService.bulkImport(new UserBulkImportRequest(List.of(valid, duplicate)));

        assertEquals(2, response.totalRequested());
        assertEquals(1, response.createdCount());
        assertEquals(1, response.errors().size());
    }

    @Test
    void exportCsvShouldReturnHeaderAndRow() {
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setTenantId(UUID.randomUUID());
        entity.setUnitId(UUID.randomUUID());
        entity.setName("Export User");
        entity.setEmail("export@shield.dev");
        entity.setPhone("9999999999");
        entity.setRole(UserRole.OWNER);
        entity.setStatus(UserStatus.ACTIVE);
        entity.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));

        when(userRepository.findAllByDeletedFalseOrderByCreatedAtDesc()).thenReturn(List.of(entity));

        String csv = userService.exportCsv();

        assertTrue(csv.startsWith("id,tenantId,unitId,name,email,phone,role,status,createdAt"));
        assertTrue(csv.contains("export@shield.dev"));
    }
}
