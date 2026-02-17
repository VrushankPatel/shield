package com.shield.module.amenities.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "amenity_booking")
public class AmenityBookingEntity extends TenantAwareEntity {

    @Column(name = "amenity_id", nullable = false, columnDefinition = "uuid")
    private UUID amenityId;

    @Column(name = "unit_id", nullable = false, columnDefinition = "uuid")
    private UUID unitId;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AmenityBookingStatus status;

    @Column(length = 500)
    private String notes;
}
