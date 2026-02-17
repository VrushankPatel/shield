package com.shield.module.utility.repository;

import com.shield.module.utility.entity.WaterTankEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaterTankRepository extends JpaRepository<WaterTankEntity, UUID> {

    Page<WaterTankEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<WaterTankEntity> findByIdAndDeletedFalse(UUID id);
}
