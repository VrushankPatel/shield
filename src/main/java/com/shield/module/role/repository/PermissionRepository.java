package com.shield.module.role.repository;

import com.shield.module.role.entity.PermissionEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {

    Optional<PermissionEntity> findByIdAndDeletedFalse(UUID id);

    Page<PermissionEntity> findAllByDeletedFalse(Pageable pageable);

    List<PermissionEntity> findAllByTenantIdAndDeletedFalse(UUID tenantId);

    Optional<PermissionEntity> findByTenantIdAndCodeIgnoreCaseAndDeletedFalse(UUID tenantId, String code);

    List<PermissionEntity> findAllByIdInAndDeletedFalse(Collection<UUID> ids);
}
