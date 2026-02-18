package com.shield.module.amenities.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "amenity")
public class AmenityEntity extends TenantAwareEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "amenity_type", length = 100)
    private String amenityType;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer capacity;

    @Column(length = 255)
    private String location;

    @Column(name = "booking_allowed", nullable = false)
    private boolean bookingAllowed = true;

    @Column(name = "advance_booking_days", nullable = false)
    private Integer advanceBookingDays = 30;

    @Column(name = "requires_approval", nullable = false)
    private boolean requiresApproval;

    @Column(nullable = false)
    private boolean active = true;
}
