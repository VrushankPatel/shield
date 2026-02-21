package com.shield.module.unit.repository;

import com.shield.module.unit.entity.UnitOwnershipHistoryEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitOwnershipHistoryRepository extends JpaRepository<UnitOwnershipHistoryEntity, UUID> {

    Page<UnitOwnershipHistoryEntity> findAllByUnitIdAndDeletedFalseOrderByChangedAtDesc(UUID unitId, Pageable pageable);
}
