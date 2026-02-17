package com.shield.module.amenities.repository;

import com.shield.module.amenities.entity.AmenityBookingEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityBookingRepository extends JpaRepository<AmenityBookingEntity, UUID> {

    Page<AmenityBookingEntity> findAllByDeletedFalse(Pageable pageable);
}
