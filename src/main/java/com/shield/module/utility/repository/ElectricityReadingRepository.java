package com.shield.module.utility.repository;

import com.shield.module.utility.entity.ElectricityReadingEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ElectricityReadingRepository extends JpaRepository<ElectricityReadingEntity, UUID> {

    Page<ElectricityReadingEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<ElectricityReadingEntity> findByIdAndDeletedFalse(UUID id);

    Page<ElectricityReadingEntity> findAllByMeterIdAndDeletedFalse(UUID meterId, Pageable pageable);

    Page<ElectricityReadingEntity> findAllByReadingDateBetweenAndDeletedFalse(LocalDate from, LocalDate to, Pageable pageable);

    Page<ElectricityReadingEntity> findAllByMeterIdAndReadingDateBetweenAndDeletedFalse(
            UUID meterId,
            LocalDate from,
            LocalDate to,
            Pageable pageable);

    List<ElectricityReadingEntity> findAllByReadingDateBetweenAndDeletedFalse(LocalDate from, LocalDate to);

    List<ElectricityReadingEntity> findAllByMeterIdAndReadingDateBetweenAndDeletedFalse(
            UUID meterId,
            LocalDate from,
            LocalDate to);
}
