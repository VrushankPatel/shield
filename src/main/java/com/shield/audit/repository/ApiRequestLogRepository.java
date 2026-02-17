package com.shield.audit.repository;

import com.shield.audit.entity.ApiRequestLogEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiRequestLogRepository extends JpaRepository<ApiRequestLogEntity, UUID> {

    Page<ApiRequestLogEntity> findAllByTenantIdAndDeletedFalse(UUID tenantId, Pageable pageable);

    Optional<ApiRequestLogEntity> findByIdAndTenantIdAndDeletedFalse(UUID id, UUID tenantId);

    Page<ApiRequestLogEntity> findAllByTenantIdAndUserIdAndDeletedFalse(UUID tenantId, UUID userId, Pageable pageable);

    Page<ApiRequestLogEntity> findAllByTenantIdAndEndpointContainingIgnoreCaseAndDeletedFalse(
            UUID tenantId,
            String endpoint,
            Pageable pageable);

    Page<ApiRequestLogEntity> findAllByTenantIdAndCreatedAtBetweenAndDeletedFalse(UUID tenantId, Instant from, Instant to, Pageable pageable);

    Page<ApiRequestLogEntity> findAllByTenantIdAndResponseTimeMsGreaterThanEqualAndDeletedFalse(
            UUID tenantId,
            Long responseTimeMs,
            Pageable pageable);

    Page<ApiRequestLogEntity> findAllByTenantIdAndResponseStatusGreaterThanEqualAndDeletedFalse(
            UUID tenantId,
            Integer responseStatus,
            Pageable pageable);

    List<ApiRequestLogEntity> findAllByTenantIdAndDeletedFalseOrderByCreatedAtDesc(UUID tenantId);
}
