package com.shield.module.asset.repository;

import com.shield.module.asset.entity.AssetCategoryEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetCategoryRepository extends JpaRepository<AssetCategoryEntity, UUID> {

    Page<AssetCategoryEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<AssetCategoryEntity> findByIdAndDeletedFalse(UUID id);
}
