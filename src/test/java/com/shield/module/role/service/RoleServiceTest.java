package com.shield.module.role.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.module.role.dto.RoleCreateRequest;
import com.shield.module.role.dto.RolePermissionAssignRequest;
import com.shield.module.role.dto.UserPermissionsResponse;
import com.shield.module.role.entity.PermissionEntity;
import com.shield.module.role.entity.RoleEntity;
import com.shield.module.role.entity.RolePermissionEntity;
import com.shield.module.role.entity.UserAdditionalRoleEntity;
import com.shield.module.role.repository.PermissionRepository;
import com.shield.module.role.repository.RolePermissionRepository;
import com.shield.module.role.repository.RoleRepository;
import com.shield.module.role.repository.UserAdditionalRoleRepository;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.entity.UserRole;
import com.shield.module.user.entity.UserStatus;
import com.shield.module.user.repository.UserRepository;
import com.shield.tenant.context.TenantContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private UserAdditionalRoleRepository userAdditionalRoleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    private RoleService roleService;

    private UUID tenantId;
    private List<RoleEntity> roleStore;
    private List<PermissionEntity> permissionStore;
    private List<RolePermissionEntity> rolePermissionStore;
    private List<UserAdditionalRoleEntity> userRoleStore;
    private AtomicLong idCounter;

    @BeforeEach
    void setUp() {
        roleService = new RoleService(
                roleRepository,
                permissionRepository,
                rolePermissionRepository,
                userAdditionalRoleRepository,
                userRepository,
                auditLogService);

        tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        roleStore = new ArrayList<>();
        permissionStore = new ArrayList<>();
        rolePermissionStore = new ArrayList<>();
        userRoleStore = new ArrayList<>();
        idCounter = new AtomicLong(1);

        configureRepositoryStubs();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void listRolesShouldSeedDefaultSystemRoles() {
        var page = roleService.listRoles(Pageable.ofSize(20));

        assertTrue(page.content().size() >= UserRole.values().length);
        assertTrue(page.content().stream().anyMatch(role -> "ADMIN".equals(role.code())));
        assertTrue(page.content().stream().anyMatch(role -> "TENANT".equals(role.code())));
    }

    @Test
    void listPermissionsShouldSeedDefaultPermissions() {
        var page = roleService.listPermissions(Pageable.ofSize(50));

        assertTrue(page.content().size() >= 10);
        assertTrue(page.content().stream().anyMatch(permission -> "USER_READ".equals(permission.code())));
    }

    @Test
    void createRoleShouldRejectDuplicateCodeWithinTenant() {
        roleService.listRoles(Pageable.ofSize(20));
        RoleCreateRequest request = new RoleCreateRequest("ADMIN", "Duplicate Admin", "Duplicate", false);

        assertThrows(BadRequestException.class, () -> roleService.createRole(request, UUID.randomUUID()));
    }

    @Test
    void assignRoleAndPermissionsShouldReflectInEffectivePermissions() {
        roleService.listRoles(Pageable.ofSize(20));
        roleService.listPermissions(Pageable.ofSize(50));

        RoleEntity customRole = new RoleEntity();
        customRole.setTenantId(tenantId);
        customRole.setCode("HELPDESK_AGENT");
        customRole.setName("Helpdesk Agent");
        customRole.setDescription("Handles tickets");
        customRole.setSystemRole(false);
        customRole = roleRepository.save(customRole);

        PermissionEntity permission = permissionStore.stream()
                .filter(item -> "USER_READ".equals(item.getCode()))
                .findFirst()
                .orElseThrow();

        roleService.assignPermissions(
                customRole.getId(),
                new RolePermissionAssignRequest(List.of(permission.getId())),
                UUID.randomUUID());

        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setTenantId(tenantId);
        user.setUnitId(UUID.randomUUID());
        user.setName("Resident User");
        user.setEmail("resident@shield.dev");
        user.setPhone("9999999999");
        user.setPasswordHash("encoded");
        user.setRole(UserRole.TENANT);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));

        roleService.assignRoleToUser(userId, customRole.getId(), UUID.randomUUID());

        UserPermissionsResponse permissions = roleService.getUserPermissions(userId);

        assertTrue(permissions.roles().contains("TENANT"));
        assertTrue(permissions.roles().contains("HELPDESK_AGENT"));
        assertTrue(permissions.permissions().contains("USER_READ"));
    }

    private void configureRepositoryStubs() {
        when(roleRepository.findAllByTenantIdAndDeletedFalse(tenantId)).thenAnswer(invocation -> new ArrayList<>(roleStore));
        when(permissionRepository.findAllByTenantIdAndDeletedFalse(tenantId)).thenAnswer(invocation -> new ArrayList<>(permissionStore));

        when(roleRepository.save(any(RoleEntity.class))).thenAnswer(invocation -> {
            RoleEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(nextId());
            }
            roleStore.removeIf(existing -> existing.getId().equals(entity.getId()));
            roleStore.add(entity);
            return entity;
        });

        when(permissionRepository.save(any(PermissionEntity.class))).thenAnswer(invocation -> {
            PermissionEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(nextId());
            }
            permissionStore.removeIf(existing -> existing.getId().equals(entity.getId()));
            permissionStore.add(entity);
            return entity;
        });

        when(rolePermissionRepository.save(any(RolePermissionEntity.class))).thenAnswer(invocation -> {
            RolePermissionEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(nextId());
            }
            rolePermissionStore.removeIf(existing -> existing.getId().equals(entity.getId()));
            rolePermissionStore.add(entity);
            return entity;
        });

        when(userAdditionalRoleRepository.save(any(UserAdditionalRoleEntity.class))).thenAnswer(invocation -> {
            UserAdditionalRoleEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(nextId());
            }
            userRoleStore.removeIf(existing -> existing.getId().equals(entity.getId()));
            userRoleStore.add(entity);
            return entity;
        });

        when(roleRepository.findAllByDeletedFalse(any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(new ArrayList<>(roleStore)));

        when(permissionRepository.findAllByDeletedFalse(any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(new ArrayList<>(permissionStore)));

        when(roleRepository.findByIdAndDeletedFalse(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            return roleStore.stream().filter(role -> id.equals(role.getId()) && !role.isDeleted()).findFirst();
        });

        when(roleRepository.existsByTenantIdAndCodeIgnoreCaseAndDeletedFalse(eq(tenantId), anyString())).thenAnswer(invocation -> {
            String code = invocation.getArgument(1, String.class).toUpperCase();
            return roleStore.stream().anyMatch(role -> !role.isDeleted() && code.equals(role.getCode().toUpperCase()));
        });

        when(permissionRepository.findByIdAndDeletedFalse(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            return permissionStore.stream().filter(permission -> id.equals(permission.getId()) && !permission.isDeleted()).findFirst();
        });

        when(rolePermissionRepository.findByRoleIdAndPermissionIdAndDeletedFalse(any(UUID.class), any(UUID.class))).thenAnswer(invocation -> {
            UUID roleId = invocation.getArgument(0);
            UUID permissionId = invocation.getArgument(1);
            return rolePermissionStore.stream()
                    .filter(mapping -> !mapping.isDeleted()
                            && roleId.equals(mapping.getRoleId())
                            && permissionId.equals(mapping.getPermissionId()))
                    .findFirst();
        });

        when(rolePermissionRepository.findAllByRoleIdInAndDeletedFalse(anyCollection())).thenAnswer(invocation -> {
            Collection<UUID> roleIds = invocation.getArgument(0);
            return rolePermissionStore.stream()
                    .filter(mapping -> !mapping.isDeleted() && roleIds.contains(mapping.getRoleId()))
                    .toList();
        });

        when(permissionRepository.findAllByIdInAndDeletedFalse(anyCollection())).thenAnswer(invocation -> {
            Collection<UUID> permissionIds = invocation.getArgument(0);
            return permissionStore.stream()
                    .filter(permission -> !permission.isDeleted() && permissionIds.contains(permission.getId()))
                    .toList();
        });

        when(userAdditionalRoleRepository.findByUserIdAndRoleIdAndDeletedFalse(any(UUID.class), any(UUID.class))).thenAnswer(invocation -> {
            UUID userId = invocation.getArgument(0);
            UUID roleId = invocation.getArgument(1);
            return userRoleStore.stream()
                    .filter(mapping -> !mapping.isDeleted() && userId.equals(mapping.getUserId()) && roleId.equals(mapping.getRoleId()))
                    .findFirst();
        });

        when(userAdditionalRoleRepository.findAllByUserIdAndDeletedFalse(any(UUID.class))).thenAnswer(invocation -> {
            UUID userId = invocation.getArgument(0);
            return userRoleStore.stream()
                    .filter(mapping -> !mapping.isDeleted() && userId.equals(mapping.getUserId()))
                    .toList();
        });
    }

    private UUID nextId() {
        return new UUID(0L, idCounter.getAndIncrement());
    }
}
