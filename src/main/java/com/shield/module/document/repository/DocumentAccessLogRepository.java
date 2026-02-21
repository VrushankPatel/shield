package com.shield.module.document.repository;

import com.shield.module.document.entity.DocumentAccessLogEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentAccessLogRepository extends JpaRepository<DocumentAccessLogEntity, UUID> {

    Page<DocumentAccessLogEntity> findAllByDeletedFalse(Pageable pageable);

    Page<DocumentAccessLogEntity> findAllByDocumentIdAndDeletedFalse(UUID documentId, Pageable pageable);

    Page<DocumentAccessLogEntity> findAllByAccessedByAndDeletedFalse(UUID accessedBy, Pageable pageable);

    Page<DocumentAccessLogEntity> findAllByAccessedAtBetweenAndDeletedFalse(Instant from, Instant to, Pageable pageable);
}
