package com.shield.module.utility.repository;

import com.shield.module.utility.entity.ElectricityMeterEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ElectricityMeterRepository extends JpaRepository<ElectricityMeterEntity, UUID> {

    Page<ElectricityMeterEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<ElectricityMeterEntity> findByIdAndDeletedFalse(UUID id);

    Page<ElectricityMeterEntity> findAllByUnitIdAndDeletedFalse(UUID unitId, Pageable pageable);

    Page<ElectricityMeterEntity> findAllByMeterTypeIgnoreCaseAndDeletedFalse(String meterType, Pageable pageable);
}
