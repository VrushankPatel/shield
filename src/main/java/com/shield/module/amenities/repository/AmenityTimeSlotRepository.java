package com.shield.module.amenities.repository;

import com.shield.module.amenities.entity.AmenityTimeSlotEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityTimeSlotRepository extends JpaRepository<AmenityTimeSlotEntity, UUID> {

    Optional<AmenityTimeSlotEntity> findByIdAndDeletedFalse(UUID id);

    List<AmenityTimeSlotEntity> findAllByAmenityIdAndDeletedFalseOrderByStartTimeAsc(UUID amenityId);
}
