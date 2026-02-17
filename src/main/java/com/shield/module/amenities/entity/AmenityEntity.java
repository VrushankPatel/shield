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

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "requires_approval", nullable = false)
    private boolean requiresApproval;
}
