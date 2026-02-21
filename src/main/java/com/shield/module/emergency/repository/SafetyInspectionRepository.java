package com.shield.module.emergency.repository;

import com.shield.module.emergency.entity.SafetyInspectionEntity;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SafetyInspectionRepository extends JpaRepository<SafetyInspectionEntity, UUID> {

    Page<SafetyInspectionEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<SafetyInspectionEntity> findByIdAndDeletedFalse(UUID id);

    Page<SafetyInspectionEntity> findAllByEquipmentIdAndDeletedFalse(UUID equipmentId, Pageable pageable);

    Page<SafetyInspectionEntity> findAllByInspectionDateBetweenAndDeletedFalse(LocalDate from, LocalDate to, Pageable pageable);
}
