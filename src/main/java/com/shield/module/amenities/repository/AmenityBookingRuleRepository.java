package com.shield.module.amenities.repository;

import com.shield.module.amenities.entity.AmenityBookingRuleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityBookingRuleRepository extends JpaRepository<AmenityBookingRuleEntity, UUID> {

    Optional<AmenityBookingRuleEntity> findByIdAndDeletedFalse(UUID id);

    List<AmenityBookingRuleEntity> findAllByAmenityIdAndDeletedFalseOrderByCreatedAtDesc(UUID amenityId);
}
