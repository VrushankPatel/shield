package com.shield.module.marketplace.repository;

import com.shield.module.marketplace.entity.MarketplaceListingEntity;
import com.shield.module.marketplace.entity.MarketplaceListingStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MarketplaceListingRepository extends JpaRepository<MarketplaceListingEntity, UUID> {

    Page<MarketplaceListingEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<MarketplaceListingEntity> findByIdAndDeletedFalse(UUID id);

    Page<MarketplaceListingEntity> findAllByCategoryIdAndDeletedFalse(UUID categoryId, Pageable pageable);

    Page<MarketplaceListingEntity> findAllByPostedByAndDeletedFalse(UUID postedBy, Pageable pageable);

    Page<MarketplaceListingEntity> findAllByStatusAndDeletedFalse(MarketplaceListingStatus status, Pageable pageable);

    Page<MarketplaceListingEntity> findAllByListingTypeIgnoreCaseAndDeletedFalse(String listingType, Pageable pageable);

    @Query("""
            SELECT l
            FROM MarketplaceListingEntity l
            WHERE l.deleted = false
              AND (
                   LOWER(l.title) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(COALESCE(l.description, '')) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            """)
    Page<MarketplaceListingEntity> searchByText(@Param("query") String query, Pageable pageable);
}
