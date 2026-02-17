package com.shield.module.analytics.repository;

import com.shield.module.analytics.entity.ReportTemplateEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportTemplateRepository extends JpaRepository<ReportTemplateEntity, UUID> {

    Page<ReportTemplateEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<ReportTemplateEntity> findByIdAndDeletedFalse(UUID id);

    Page<ReportTemplateEntity> findAllByReportTypeAndDeletedFalse(String reportType, Pageable pageable);
}
