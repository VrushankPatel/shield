package com.shield.module.amenities.repository;

import com.shield.module.amenities.entity.AmenityPricingEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityPricingRepository extends JpaRepository<AmenityPricingEntity, UUID> {

    Optional<AmenityPricingEntity> findByIdAndDeletedFalse(UUID id);

    List<AmenityPricingEntity> findAllByAmenityIdAndDeletedFalseOrderByCreatedAtDesc(UUID amenityId);
}
