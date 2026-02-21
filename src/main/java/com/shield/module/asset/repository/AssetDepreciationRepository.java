package com.shield.module.asset.repository;

import com.shield.module.asset.entity.AssetDepreciationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetDepreciationRepository extends JpaRepository<AssetDepreciationEntity, UUID> {

    List<AssetDepreciationEntity> findAllByAssetIdAndDeletedFalseOrderByDepreciationYearDesc(UUID assetId);

    List<AssetDepreciationEntity> findAllByDepreciationYearAndDeletedFalseOrderByAssetIdAsc(Integer year);

    List<AssetDepreciationEntity> findAllByDeletedFalseOrderByDepreciationYearDesc();
}
