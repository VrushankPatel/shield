package com.shield.module.emergency.repository;

import com.shield.module.emergency.entity.SafetyEquipmentEntity;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SafetyEquipmentRepository extends JpaRepository<SafetyEquipmentEntity, UUID> {

    Page<SafetyEquipmentEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<SafetyEquipmentEntity> findByIdAndDeletedFalse(UUID id);

    Page<SafetyEquipmentEntity> findAllByEquipmentTypeIgnoreCaseAndDeletedFalse(String equipmentType, Pageable pageable);

    Page<SafetyEquipmentEntity> findAllByNextInspectionDateLessThanEqualAndDeletedFalse(LocalDate date, Pageable pageable);
}
