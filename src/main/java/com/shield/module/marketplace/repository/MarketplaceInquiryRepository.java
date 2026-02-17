package com.shield.module.marketplace.repository;

import com.shield.module.marketplace.entity.MarketplaceInquiryEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketplaceInquiryRepository extends JpaRepository<MarketplaceInquiryEntity, UUID> {

    Page<MarketplaceInquiryEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<MarketplaceInquiryEntity> findByIdAndDeletedFalse(UUID id);

    Page<MarketplaceInquiryEntity> findAllByListingIdAndDeletedFalse(UUID listingId, Pageable pageable);

    Page<MarketplaceInquiryEntity> findAllByInquiredByAndDeletedFalse(UUID inquiredBy, Pageable pageable);
}
