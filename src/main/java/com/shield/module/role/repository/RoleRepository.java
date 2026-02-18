package com.shield.module.role.repository;

import com.shield.module.role.entity.RoleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    Optional<RoleEntity> findByIdAndDeletedFalse(UUID id);

    Page<RoleEntity> findAllByDeletedFalse(Pageable pageable);

    List<RoleEntity> findAllByTenantIdAndDeletedFalse(UUID tenantId);

    Optional<RoleEntity> findByTenantIdAndCodeIgnoreCaseAndDeletedFalse(UUID tenantId, String code);

    boolean existsByTenantIdAndCodeIgnoreCaseAndDeletedFalse(UUID tenantId, String code);
}
