package com.shield.module.amenities.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "amenity_booking_rule")
public class AmenityBookingRuleEntity extends TenantAwareEntity {

    @Column(name = "amenity_id", nullable = false, columnDefinition = "uuid")
    private UUID amenityId;

    @Column(name = "rule_type", nullable = false, length = 100)
    private String ruleType;

    @Column(name = "rule_value", nullable = false, length = 255)
    private String ruleValue;

    @Column(nullable = false)
    private boolean active = true;
}
