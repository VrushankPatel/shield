package com.shield.module.visitor.repository;

import com.shield.module.visitor.entity.VisitorEntryExitLogEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitorEntryExitLogRepository extends JpaRepository<VisitorEntryExitLogEntity, UUID> {

    Optional<VisitorEntryExitLogEntity> findByIdAndDeletedFalse(UUID id);

    Page<VisitorEntryExitLogEntity> findAllByDeletedFalse(Pageable pageable);

    Page<VisitorEntryExitLogEntity> findAllByVisitorPassIdAndDeletedFalse(UUID visitorPassId, Pageable pageable);

    Page<VisitorEntryExitLogEntity> findAllByEntryTimeBetweenAndDeletedFalse(Instant from, Instant to, Pageable pageable);

    Page<VisitorEntryExitLogEntity> findAllByExitTimeIsNullAndDeletedFalse(Pageable pageable);

    Optional<VisitorEntryExitLogEntity> findFirstByVisitorPassIdAndExitTimeIsNullAndDeletedFalseOrderByEntryTimeDesc(UUID visitorPassId);
}
