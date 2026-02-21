package com.shield.module.role.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.role.dto.PermissionResponse;
import com.shield.module.role.dto.RoleCreateRequest;
import com.shield.module.role.dto.RolePermissionAssignRequest;
import com.shield.module.role.dto.RoleResponse;
import com.shield.module.role.dto.RoleUpdateRequest;
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
import com.shield.module.user.repository.UserRepository;
import com.shield.tenant.context.TenantContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private static final String ENTITY_APP_ROLE = "app_role";
    private static final String ENTITY_USERS = "users";
    private static final String MODULE_IDENTITY = "IDENTITY";
    private static final String PERMISSION_USER_READ = "USER_READ";
    private static final String PERMISSION_USER_WRITE = "USER_WRITE";
    private static final String PERMISSION_UNIT_READ = "UNIT_READ";
    private static final String PERMISSION_UNIT_WRITE = "UNIT_WRITE";
    private static final String PERMISSION_ANNOUNCEMENT_READ = "ANNOUNCEMENT_READ";
    private static final String PERMISSION_ANNOUNCEMENT_MANAGE = "ANNOUNCEMENT_MANAGE";
    private static final String PERMISSION_VISITOR_MANAGE = "VISITOR_MANAGE";
    private static final String PERMISSION_COMPLAINT_CREATE = "COMPLAINT_CREATE";
    private static final String PERMISSION_ASSET_MANAGE = "ASSET_MANAGE";
    private static final String PERMISSION_AMENITY_BOOK = "AMENITY_BOOK";
    private static final String PERMISSION_AMENITY_MANAGE = "AMENITY_MANAGE";
    private static final String PERMISSION_BILLING_MANAGE = "BILLING_MANAGE";
    private static final String PERMISSION_MEETING_MANAGE = "MEETING_MANAGE";
    private static final String PERMISSION_DIGITAL_ID_VERIFY = "DIGITAL_ID_VERIFY";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserAdditionalRoleRepository userAdditionalRoleRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public PagedResponse<RoleResponse> listRoles(Pageable pageable) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        ensureDefaults(tenantId);
        return PagedResponse.from(roleRepository.findAllByDeletedFalse(pageable).map(this::toRoleResponse));
    }

    public RoleResponse getRole(UUID id) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        ensureDefaults(tenantId);
        RoleEntity role = findRole(id);
        return toRoleResponse(role);
    }

    public RoleResponse createRole(RoleCreateRequest request, UUID actorUserId) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        ensureDefaults(tenantId);

        String normalizedCode = normalizeCode(request.code());
        if (roleRepository.existsByTenantIdAndCodeIgnoreCaseAndDeletedFalse(tenantId, normalizedCode)) {
            throw new BadRequestException("Role code already exists: " + normalizedCode);
        }

        RoleEntity role = new RoleEntity();
        role.setTenantId(tenantId);
        role.setCode(normalizedCode);
        role.setName(request.name().trim());
        role.setDescription(request.description());
        role.setSystemRole(request.systemRole());

        RoleEntity saved = roleRepository.save(role);
        auditLogService.logEvent(tenantId, actorUserId, "ROLE_CREATED", ENTITY_APP_ROLE, saved.getId(), null);
        return toRoleResponse(saved);
    }

    public RoleResponse updateRole(UUID id, RoleUpdateRequest request, UUID actorUserId) {
        RoleEntity role = findRole(id);
        role.setName(request.name().trim());
        role.setDescription(request.description());

        RoleEntity saved = roleRepository.save(role);
        auditLogService.logEvent(saved.getTenantId(), actorUserId, "ROLE_UPDATED", ENTITY_APP_ROLE, saved.getId(), null);
        return toRoleResponse(saved);
    }

    public void deleteRole(UUID id, UUID actorUserId) {
        RoleEntity role = findRole(id);
        if (role.isSystemRole()) {
            throw new BadRequestException("System role cannot be deleted");
        }

        role.setDeleted(true);
        roleRepository.save(role);
        auditLogService.logEvent(role.getTenantId(), actorUserId, "ROLE_DELETED", ENTITY_APP_ROLE, role.getId(), null);
    }

    public PagedResponse<PermissionResponse> listPermissions(Pageable pageable) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        ensureDefaults(tenantId);
        return PagedResponse.from(permissionRepository.findAllByDeletedFalse(pageable).map(this::toPermissionResponse));
    }

    public void assignPermissions(UUID roleId, RolePermissionAssignRequest request, UUID actorUserId) {
        RoleEntity role = findRole(roleId);
        for (UUID permissionId : request.permissionIds()) {
            PermissionEntity permission = permissionRepository.findByIdAndDeletedFalse(permissionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionId));

            boolean exists = rolePermissionRepository.findByRoleIdAndPermissionIdAndDeletedFalse(roleId, permission.getId()).isPresent();
            if (exists) {
                continue;
            }

            RolePermissionEntity mapping = new RolePermissionEntity();
            mapping.setTenantId(role.getTenantId());
            mapping.setRoleId(roleId);
            mapping.setPermissionId(permission.getId());
            rolePermissionRepository.save(mapping);
        }
        auditLogService.logEvent(role.getTenantId(), actorUserId, "ROLE_PERMISSIONS_ASSIGNED", ENTITY_APP_ROLE, roleId, null);
    }

    public void removePermission(UUID roleId, UUID permissionId, UUID actorUserId) {
        RoleEntity role = findRole(roleId);
        RolePermissionEntity mapping = rolePermissionRepository.findByRoleIdAndPermissionIdAndDeletedFalse(roleId, permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Role permission mapping not found"));
        mapping.setDeleted(true);
        rolePermissionRepository.save(mapping);
        auditLogService.logEvent(role.getTenantId(), actorUserId, "ROLE_PERMISSION_REMOVED", ENTITY_APP_ROLE, roleId, null);
    }

    public void assignRoleToUser(UUID userId, UUID roleId, UUID actorUserId) {
        UserEntity user = findUser(userId);
        RoleEntity role = findRole(roleId);

        if (user.getRole().name().equalsIgnoreCase(role.getCode())) {
            throw new BadRequestException("Role is already assigned as the user's primary role");
        }

        boolean exists = userAdditionalRoleRepository.findByUserIdAndRoleIdAndDeletedFalse(userId, roleId).isPresent();
        if (exists) {
            throw new BadRequestException("Role is already assigned to the user");
        }

        UserAdditionalRoleEntity assignment = new UserAdditionalRoleEntity();
        assignment.setTenantId(user.getTenantId());
        assignment.setUserId(userId);
        assignment.setRoleId(roleId);
        assignment.setGrantedBy(actorUserId);
        assignment.setGrantedAt(Instant.now());
        userAdditionalRoleRepository.save(assignment);

        auditLogService.logEvent(user.getTenantId(), actorUserId, "USER_ROLE_ASSIGNED", ENTITY_USERS, userId, null);
    }

    public void removeRoleFromUser(UUID userId, UUID roleId, UUID actorUserId) {
        UserEntity user = findUser(userId);
        UserAdditionalRoleEntity assignment = userAdditionalRoleRepository.findByUserIdAndRoleIdAndDeletedFalse(userId, roleId)
                .orElseThrow(() -> new ResourceNotFoundException("User role mapping not found"));
        assignment.setDeleted(true);
        userAdditionalRoleRepository.save(assignment);

        auditLogService.logEvent(user.getTenantId(), actorUserId, "USER_ROLE_REMOVED", ENTITY_USERS, userId, null);
    }

    public UserPermissionsResponse getUserPermissions(UUID userId) {
        UserEntity user = findUser(userId);
        ensureDefaults(user.getTenantId());

        List<RoleEntity> tenantRoles = roleRepository.findAllByTenantIdAndDeletedFalse(user.getTenantId());
        Map<UUID, RoleEntity> roleById = tenantRoles.stream().collect(Collectors.toMap(RoleEntity::getId, role -> role));
        Map<String, RoleEntity> roleByCode = tenantRoles.stream()
                .collect(Collectors.toMap(role -> role.getCode().toUpperCase(), role -> role, (left, right) -> left, LinkedHashMap::new));

        Set<String> roleCodes = new LinkedHashSet<>();
        Set<UUID> effectiveRoleIds = new LinkedHashSet<>();

        String primaryRoleCode = user.getRole().name();
        roleCodes.add(primaryRoleCode);
        RoleEntity primaryRole = roleByCode.get(primaryRoleCode.toUpperCase());
        if (primaryRole != null) {
            effectiveRoleIds.add(primaryRole.getId());
        }

        List<UserAdditionalRoleEntity> additionalRoles = userAdditionalRoleRepository.findAllByUserIdAndDeletedFalse(userId);
        for (UserAdditionalRoleEntity additionalRole : additionalRoles) {
            RoleEntity role = roleById.get(additionalRole.getRoleId());
            if (role != null && !role.isDeleted()) {
                roleCodes.add(role.getCode());
                effectiveRoleIds.add(role.getId());
            }
        }

        List<String> permissionCodes = resolvePermissionCodes(effectiveRoleIds);
        return new UserPermissionsResponse(userId, new ArrayList<>(roleCodes), permissionCodes);
    }

    private List<String> resolvePermissionCodes(Collection<UUID> roleIds) {
        if (roleIds.isEmpty()) {
            return List.of();
        }

        Set<UUID> permissionIds = rolePermissionRepository.findAllByRoleIdInAndDeletedFalse(roleIds)
                .stream()
                .map(RolePermissionEntity::getPermissionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (permissionIds.isEmpty()) {
            return List.of();
        }

        return permissionRepository.findAllByIdInAndDeletedFalse(permissionIds)
                .stream()
                .map(PermissionEntity::getCode)
                .sorted()
                .toList();
    }

    private RoleEntity findRole(UUID roleId) {
        return roleRepository.findByIdAndDeletedFalse(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
    }

    private UserEntity findUser(UUID userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private RoleResponse toRoleResponse(RoleEntity role) {
        return new RoleResponse(
                role.getId(),
                role.getTenantId(),
                role.getCode(),
                role.getName(),
                role.getDescription(),
                role.isSystemRole(),
                role.getCreatedAt(),
                role.getUpdatedAt());
    }

    private PermissionResponse toPermissionResponse(PermissionEntity permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getTenantId(),
                permission.getCode(),
                permission.getModuleName(),
                permission.getDescription(),
                permission.getCreatedAt(),
                permission.getUpdatedAt());
    }

    private String normalizeCode(String code) {
        return code.trim().replace('-', '_').replace(' ', '_').toUpperCase();
    }

    private void ensureDefaults(UUID tenantId) {
        Map<String, RoleEntity> roleByCode = ensureDefaultRoles(tenantId);
        Map<String, PermissionEntity> permissionByCode = ensureDefaultPermissions(tenantId);
        ensureDefaultRolePermissionMapping(tenantId, roleByCode, permissionByCode);
    }

    private Map<String, RoleEntity> ensureDefaultRoles(UUID tenantId) {
        List<RoleEntity> existingRoles = roleRepository.findAllByTenantIdAndDeletedFalse(tenantId);
        Map<String, RoleEntity> roleByCode = existingRoles.stream()
                .collect(Collectors.toMap(role -> role.getCode().toUpperCase(), role -> role, (left, right) -> left, LinkedHashMap::new));

        for (UserRole userRole : UserRole.values()) {
            String code = userRole.name();
            if (roleByCode.containsKey(code)) {
                continue;
            }
            RoleEntity role = new RoleEntity();
            role.setTenantId(tenantId);
            role.setCode(code);
            role.setName(code);
            role.setDescription("System role: " + code);
            role.setSystemRole(true);
            RoleEntity saved = roleRepository.save(role);
            roleByCode.put(saved.getCode().toUpperCase(), saved);
        }
        return roleByCode;
    }

    private Map<String, PermissionEntity> ensureDefaultPermissions(UUID tenantId) {
        List<PermissionEntity> existingPermissions = permissionRepository.findAllByTenantIdAndDeletedFalse(tenantId);
        Map<String, PermissionEntity> permissionByCode = existingPermissions.stream()
                .collect(Collectors.toMap(permission -> permission.getCode().toUpperCase(), permission -> permission,
                        (left, right) -> left, LinkedHashMap::new));

        for (PermissionSeed seed : defaultPermissionSeeds()) {
            if (permissionByCode.containsKey(seed.code())) {
                continue;
            }
            PermissionEntity permission = new PermissionEntity();
            permission.setTenantId(tenantId);
            permission.setCode(seed.code());
            permission.setModuleName(seed.moduleName());
            permission.setDescription(seed.description());
            PermissionEntity saved = permissionRepository.save(permission);
            permissionByCode.put(saved.getCode().toUpperCase(), saved);
        }
        return permissionByCode;
    }

    private void ensureDefaultRolePermissionMapping(
            UUID tenantId,
            Map<String, RoleEntity> roleByCode,
            Map<String, PermissionEntity> permissionByCode) {
        Map<String, List<String>> defaultMapping = defaultRolePermissionMapping();
        for (Map.Entry<String, List<String>> entry : defaultMapping.entrySet()) {
            RoleEntity role = roleByCode.get(entry.getKey());
            if (role != null) {
                for (String permissionCode : entry.getValue()) {
                    PermissionEntity permission = permissionByCode.get(permissionCode);
                    if (permission != null) {
                        assignRolePermissionIfMissing(tenantId, role.getId(), permission.getId());
                    }
                }
            }
        }
    }

    private void assignRolePermissionIfMissing(UUID tenantId, UUID roleId, UUID permissionId) {
        boolean exists = rolePermissionRepository.findByRoleIdAndPermissionIdAndDeletedFalse(roleId, permissionId).isPresent();
        if (!exists) {
            RolePermissionEntity mapping = new RolePermissionEntity();
            mapping.setTenantId(tenantId);
            mapping.setRoleId(roleId);
            mapping.setPermissionId(permissionId);
            rolePermissionRepository.save(mapping);
        }
    }

    private List<PermissionSeed> defaultPermissionSeeds() {
        return List.of(
                new PermissionSeed(PERMISSION_USER_READ, MODULE_IDENTITY, "Read users"),
                new PermissionSeed(PERMISSION_USER_WRITE, MODULE_IDENTITY, "Create and update users"),
                new PermissionSeed(PERMISSION_UNIT_READ, MODULE_IDENTITY, "Read units"),
                new PermissionSeed(PERMISSION_UNIT_WRITE, MODULE_IDENTITY, "Create and update units"),
                new PermissionSeed(PERMISSION_ANNOUNCEMENT_READ, "COMMUNICATION", "Read announcements"),
                new PermissionSeed(PERMISSION_ANNOUNCEMENT_MANAGE, "COMMUNICATION", "Manage announcements"),
                new PermissionSeed(PERMISSION_VISITOR_MANAGE, "VISITOR", "Manage visitor flow"),
                new PermissionSeed(PERMISSION_COMPLAINT_CREATE, "COMPLAINT", "Create complaints"),
                new PermissionSeed(PERMISSION_ASSET_MANAGE, "ASSET", "Manage assets"),
                new PermissionSeed(PERMISSION_AMENITY_BOOK, "AMENITIES", "Book amenities"),
                new PermissionSeed(PERMISSION_AMENITY_MANAGE, "AMENITIES", "Manage amenities"),
                new PermissionSeed(PERMISSION_BILLING_MANAGE, "BILLING", "Manage bills and payments"),
                new PermissionSeed(PERMISSION_MEETING_MANAGE, "MEETING", "Manage meetings"),
                new PermissionSeed(PERMISSION_DIGITAL_ID_VERIFY, MODULE_IDENTITY, "Verify digital IDs")
        );
    }

    private Map<String, List<String>> defaultRolePermissionMapping() {
        Map<String, List<String>> mapping = new LinkedHashMap<>();

        List<String> allPermissionCodes = defaultPermissionSeeds().stream()
                .map(PermissionSeed::code)
                .toList();

        mapping.put("ADMIN", allPermissionCodes);
        mapping.put("COMMITTEE", List.of(
                PERMISSION_USER_READ, PERMISSION_USER_WRITE, PERMISSION_UNIT_READ, PERMISSION_UNIT_WRITE, PERMISSION_ANNOUNCEMENT_MANAGE,
                PERMISSION_VISITOR_MANAGE, PERMISSION_COMPLAINT_CREATE, PERMISSION_ASSET_MANAGE, PERMISSION_AMENITY_MANAGE,
                PERMISSION_BILLING_MANAGE, PERMISSION_MEETING_MANAGE, PERMISSION_DIGITAL_ID_VERIFY));
        mapping.put("OWNER", List.of(
                PERMISSION_USER_READ, PERMISSION_UNIT_READ, PERMISSION_ANNOUNCEMENT_READ, PERMISSION_COMPLAINT_CREATE, PERMISSION_AMENITY_BOOK));
        mapping.put("TENANT", List.of(
                PERMISSION_USER_READ, PERMISSION_UNIT_READ, PERMISSION_ANNOUNCEMENT_READ, PERMISSION_COMPLAINT_CREATE, PERMISSION_AMENITY_BOOK));
        mapping.put("SECURITY", List.of(
                PERMISSION_ANNOUNCEMENT_READ, PERMISSION_VISITOR_MANAGE, PERMISSION_DIGITAL_ID_VERIFY));

        return mapping;
    }

    private record PermissionSeed(String code, String moduleName, String description) {
    }
}
