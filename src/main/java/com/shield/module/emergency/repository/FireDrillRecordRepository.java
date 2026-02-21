package com.shield.module.emergency.repository;

import com.shield.module.emergency.entity.FireDrillRecordEntity;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FireDrillRecordRepository extends JpaRepository<FireDrillRecordEntity, UUID> {

    Page<FireDrillRecordEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<FireDrillRecordEntity> findByIdAndDeletedFalse(UUID id);

    Page<FireDrillRecordEntity> findAllByDrillDateBetweenAndDeletedFalse(LocalDate from, LocalDate to, Pageable pageable);
}
