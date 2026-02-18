package com.shield.module.amenities.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "amenity_pricing")
public class AmenityPricingEntity extends TenantAwareEntity {

    @Column(name = "amenity_id", nullable = false, columnDefinition = "uuid")
    private UUID amenityId;

    @Column(name = "time_slot_id", nullable = false, columnDefinition = "uuid")
    private UUID timeSlotId;

    @Column(name = "day_type", nullable = false, length = 50)
    private String dayType;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "peak_hour", nullable = false)
    private boolean peakHour;

    @Column(name = "peak_hour_multiplier", precision = 5, scale = 2)
    private BigDecimal peakHourMultiplier;
}
