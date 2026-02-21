package com.shield.module.asset.repository;

import com.shield.module.asset.entity.AssetEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<AssetEntity, UUID> {

    Page<AssetEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<AssetEntity> findByIdAndDeletedFalse(UUID id);

    Page<AssetEntity> findAllByCategoryIdAndDeletedFalse(UUID categoryId, Pageable pageable);

    Page<AssetEntity> findAllByLocationContainingIgnoreCaseAndDeletedFalse(String location, Pageable pageable);

    Optional<AssetEntity> findByAssetCodeAndDeletedFalse(String assetCode);

    Optional<AssetEntity> findByQrCodeDataAndDeletedFalse(String qrCodeData);

    Page<AssetEntity> findAllByAmcApplicableTrueAndAmcEndDateBetweenAndDeletedFalse(
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable);

    Page<AssetEntity> findAllByWarrantyExpiryDateBetweenAndDeletedFalse(
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable);

    List<AssetEntity> findAllByDeletedFalseOrderByCreatedAtDesc();
}
