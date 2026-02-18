package com.shield.module.visitor.repository;

import com.shield.module.visitor.entity.VisitorPassEntity;
import com.shield.module.visitor.entity.VisitorPassStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VisitorPassRepository extends JpaRepository<VisitorPassEntity, UUID> {

    Optional<VisitorPassEntity> findByIdAndDeletedFalse(UUID id);

    Page<VisitorPassEntity> findAllByDeletedFalse(Pageable pageable);

    Page<VisitorPassEntity> findAllByUnitIdAndDeletedFalse(UUID unitId, Pageable pageable);

    Page<VisitorPassEntity> findAllByVisitDateAndDeletedFalse(LocalDate visitDate, Pageable pageable);

    Page<VisitorPassEntity> findAllByStatusAndDeletedFalse(VisitorPassStatus status, Pageable pageable);

    Optional<VisitorPassEntity> findByQrCodeAndDeletedFalse(String qrCode);

    boolean existsByPassNumber(String passNumber);

    Page<VisitorPassEntity> findAllByStatusInAndValidToGreaterThanAndDeletedFalse(
            java.util.Collection<VisitorPassStatus> statuses,
            Instant now,
            Pageable pageable);
}
