package com.shield.module.unit.repository;

import com.shield.module.unit.entity.UnitEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitRepository extends JpaRepository<UnitEntity, UUID> {

    Page<UnitEntity> findAllByDeletedFalse(Pageable pageable);

    java.util.Optional<UnitEntity> findByIdAndDeletedFalse(UUID id);
}
