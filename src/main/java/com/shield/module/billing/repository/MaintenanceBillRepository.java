package com.shield.module.billing.repository;

import com.shield.module.billing.entity.MaintenanceBillEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceBillRepository extends JpaRepository<MaintenanceBillEntity, UUID> {

    List<MaintenanceBillEntity> findByUnitIdAndDeletedFalse(UUID unitId);

    Optional<MaintenanceBillEntity> findByIdAndDeletedFalse(UUID id);
}
