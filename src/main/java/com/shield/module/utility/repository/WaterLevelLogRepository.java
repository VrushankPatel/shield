package com.shield.module.utility.repository;

import com.shield.module.utility.entity.WaterLevelLogEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaterLevelLogRepository extends JpaRepository<WaterLevelLogEntity, UUID> {

    Page<WaterLevelLogEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<WaterLevelLogEntity> findByIdAndDeletedFalse(UUID id);

    Page<WaterLevelLogEntity> findAllByTankIdAndDeletedFalse(UUID tankId, Pageable pageable);

    Optional<WaterLevelLogEntity> findTopByDeletedFalseOrderByReadingTimeDesc();

    Optional<WaterLevelLogEntity> findTopByTankIdAndDeletedFalseOrderByReadingTimeDesc(UUID tankId);

    Page<WaterLevelLogEntity> findAllByReadingTimeBetweenAndDeletedFalse(Instant from, Instant to, Pageable pageable);

    List<WaterLevelLogEntity> findAllByTankIdAndReadingTimeBetweenAndDeletedFalse(UUID tankId, Instant from, Instant to);
}
