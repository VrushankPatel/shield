package com.shield.audit.repository;

import com.shield.audit.entity.SystemLogEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemLogRepository extends JpaRepository<SystemLogEntity, UUID> {

    Page<SystemLogEntity> findAllByTenantIdAndDeletedFalse(UUID tenantId, Pageable pageable);

    Optional<SystemLogEntity> findByIdAndTenantIdAndDeletedFalse(UUID id, UUID tenantId);

    Page<SystemLogEntity> findAllByTenantIdAndLogLevelAndDeletedFalse(UUID tenantId, String logLevel, Pageable pageable);

    Page<SystemLogEntity> findAllByTenantIdAndCreatedAtBetweenAndDeletedFalse(UUID tenantId, Instant from, Instant to, Pageable pageable);

    List<SystemLogEntity> findAllByTenantIdAndDeletedFalseOrderByCreatedAtDesc(UUID tenantId);
}
