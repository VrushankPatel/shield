package com.shield.module.marketplace.repository;

import com.shield.module.marketplace.entity.CarpoolListingEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarpoolListingRepository extends JpaRepository<CarpoolListingEntity, UUID> {

    Page<CarpoolListingEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<CarpoolListingEntity> findByIdAndDeletedFalse(UUID id);

    Page<CarpoolListingEntity> findAllByPostedByAndDeletedFalse(UUID postedBy, Pageable pageable);

    Page<CarpoolListingEntity> findAllByRouteFromIgnoreCaseAndRouteToIgnoreCaseAndDeletedFalse(
            String routeFrom,
            String routeTo,
            Pageable pageable);
}
