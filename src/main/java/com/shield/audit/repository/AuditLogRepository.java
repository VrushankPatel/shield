package com.shield.audit.repository;

import com.shield.audit.entity.AuditLogEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {

    Page<AuditLogEntity> findAllByTenantIdAndDeletedFalse(UUID tenantId, Pageable pageable);

    Optional<AuditLogEntity> findByIdAndTenantIdAndDeletedFalse(UUID id, UUID tenantId);

    Page<AuditLogEntity> findAllByTenantIdAndUserIdAndDeletedFalse(UUID tenantId, UUID userId, Pageable pageable);

    Page<AuditLogEntity> findAllByTenantIdAndEntityTypeIgnoreCaseAndEntityIdAndDeletedFalse(
            UUID tenantId,
            String entityType,
            UUID entityId,
            Pageable pageable);

    Page<AuditLogEntity> findAllByTenantIdAndActionAndDeletedFalse(UUID tenantId, String action, Pageable pageable);

    Page<AuditLogEntity> findAllByTenantIdAndCreatedAtBetweenAndDeletedFalse(UUID tenantId, Instant from, Instant to, Pageable pageable);

    List<AuditLogEntity> findAllByTenantIdAndDeletedFalseOrderByCreatedAtDesc(UUID tenantId);
}
