package com.shield.module.analytics.repository;

import com.shield.module.analytics.entity.ScheduledReportEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduledReportRepository extends JpaRepository<ScheduledReportEntity, UUID> {

    Page<ScheduledReportEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<ScheduledReportEntity> findByIdAndDeletedFalse(UUID id);
}
