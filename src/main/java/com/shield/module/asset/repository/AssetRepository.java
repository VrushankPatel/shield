package com.shield.module.asset.repository;

import com.shield.module.asset.entity.AssetEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<AssetEntity, UUID> {

    Page<AssetEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<AssetEntity> findByIdAndDeletedFalse(UUID id);
}
