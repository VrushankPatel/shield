package com.shield.module.parking.entity;

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
@Table(name = "parking_slot")
public class ParkingSlotEntity extends TenantAwareEntity {

    @Column(name = "slot_number", nullable = false, length = 50)
    private String slotNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "parking_type", nullable = false, length = 50)
    private ParkingType parkingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 50)
    private VehicleType vehicleType;

    @Column(name = "unit_id", columnDefinition = "uuid")
    private UUID unitId;

    @Column(nullable = false)
    private boolean allocated;

    @Column(name = "allocated_at")
    private Instant allocatedAt;
}
