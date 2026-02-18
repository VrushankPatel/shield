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
@Table(name = "amenity_cancellation_policy")
public class AmenityCancellationPolicyEntity extends TenantAwareEntity {

    @Column(name = "amenity_id", nullable = false, columnDefinition = "uuid")
    private UUID amenityId;

    @Column(name = "days_before_booking", nullable = false)
    private Integer daysBeforeBooking;

    @Column(name = "refund_percentage", nullable = false, precision = 6, scale = 2)
    private BigDecimal refundPercentage;
}
