package com.shield.module.emergency.repository;

import com.shield.module.emergency.entity.SosAlertEntity;
import com.shield.module.emergency.entity.SosAlertStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SosAlertRepository extends JpaRepository<SosAlertEntity, UUID> {

    Page<SosAlertEntity> findAllByDeletedFalse(Pageable pageable);

    Page<SosAlertEntity> findAllByStatusAndDeletedFalse(SosAlertStatus status, Pageable pageable);

    Page<SosAlertEntity> findAllByAlertTypeIgnoreCaseAndDeletedFalse(String alertType, Pageable pageable);

    Page<SosAlertEntity> findAllByCreatedAtBetweenAndDeletedFalse(Instant from, Instant to, Pageable pageable);

    Optional<SosAlertEntity> findByIdAndDeletedFalse(UUID id);
}
