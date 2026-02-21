package com.shield.module.amenities.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "amenity_time_slot")
public class AmenityTimeSlotEntity extends TenantAwareEntity {

    @Column(name = "amenity_id", nullable = false, columnDefinition = "uuid")
    private UUID amenityId;

    @Column(name = "slot_name", nullable = false, length = 100)
    private String slotName;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private boolean active = true;
}
