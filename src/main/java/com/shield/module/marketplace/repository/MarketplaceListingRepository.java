package com.shield.module.marketplace.repository;

import com.shield.module.marketplace.entity.MarketplaceListingEntity;
import com.shield.module.marketplace.entity.MarketplaceListingStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketplaceListingRepository extends JpaRepository<MarketplaceListingEntity, UUID> {

    Page<MarketplaceListingEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<MarketplaceListingEntity> findByIdAndDeletedFalse(UUID id);

    Page<MarketplaceListingEntity> findAllByCategoryIdAndDeletedFalse(UUID categoryId, Pageable pageable);

    Page<MarketplaceListingEntity> findAllByPostedByAndDeletedFalse(UUID postedBy, Pageable pageable);

    Page<MarketplaceListingEntity> findAllByStatusAndDeletedFalse(MarketplaceListingStatus status, Pageable pageable);
}
