package com.shield.module.role.repository;

import com.shield.module.role.entity.RolePermissionEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, UUID> {

    List<RolePermissionEntity> findAllByRoleIdAndDeletedFalse(UUID roleId);

    List<RolePermissionEntity> findAllByRoleIdInAndDeletedFalse(Collection<UUID> roleIds);

    Optional<RolePermissionEntity> findByRoleIdAndPermissionIdAndDeletedFalse(UUID roleId, UUID permissionId);
}
