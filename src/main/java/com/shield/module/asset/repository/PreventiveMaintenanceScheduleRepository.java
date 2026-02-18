package com.shield.module.asset.repository;

import com.shield.module.asset.entity.PreventiveMaintenanceScheduleEntity;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreventiveMaintenanceScheduleRepository extends JpaRepository<PreventiveMaintenanceScheduleEntity, UUID> {

    Page<PreventiveMaintenanceScheduleEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<PreventiveMaintenanceScheduleEntity> findByIdAndDeletedFalse(UUID id);

    Page<PreventiveMaintenanceScheduleEntity> findAllByAssetIdAndDeletedFalse(UUID assetId, Pageable pageable);

    Page<PreventiveMaintenanceScheduleEntity> findAllByActiveTrueAndNextMaintenanceDateLessThanEqualAndDeletedFalse(
            LocalDate dueDate,
            Pageable pageable);
}
