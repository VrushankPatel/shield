package com.shield.module.amenities.repository;

import com.shield.module.amenities.entity.AmenityBookingEntity;
import com.shield.module.amenities.entity.AmenityBookingStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityBookingRepository extends JpaRepository<AmenityBookingEntity, UUID> {

    Page<AmenityBookingEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<AmenityBookingEntity> findByIdAndDeletedFalse(UUID id);

    Page<AmenityBookingEntity> findAllByAmenityIdAndDeletedFalse(UUID amenityId, Pageable pageable);

    Page<AmenityBookingEntity> findAllByBookedByAndDeletedFalse(UUID bookedBy, Pageable pageable);

    Page<AmenityBookingEntity> findAllByStatusAndDeletedFalse(AmenityBookingStatus status, Pageable pageable);

    Page<AmenityBookingEntity> findAllByBookingDateAndDeletedFalse(LocalDate bookingDate, Pageable pageable);

    long countByAmenityIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusInAndDeletedFalse(
            UUID amenityId,
            Instant endTime,
            Instant startTime,
            Iterable<AmenityBookingStatus> statuses);

    long countByAmenityIdAndIdNotAndStartTimeLessThanAndEndTimeGreaterThanAndStatusInAndDeletedFalse(
            UUID amenityId,
            UUID bookingId,
            Instant endTime,
            Instant startTime,
            Iterable<AmenityBookingStatus> statuses);
}
