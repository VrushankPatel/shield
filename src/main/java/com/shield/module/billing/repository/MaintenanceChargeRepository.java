package com.shield.module.billing.repository;

import com.shield.module.billing.entity.MaintenanceChargeEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceChargeRepository extends JpaRepository<MaintenanceChargeEntity, UUID> {

    Optional<MaintenanceChargeEntity> findByIdAndDeletedFalse(UUID id);

    List<MaintenanceChargeEntity> findAllByDeletedFalse();

    List<MaintenanceChargeEntity> findAllByBillingCycleIdAndDeletedFalse(UUID billingCycleId);

    List<MaintenanceChargeEntity> findAllByUnitIdAndDeletedFalse(UUID unitId);
}
