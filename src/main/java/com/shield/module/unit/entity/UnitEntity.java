package com.shield.module.unit.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "unit")
public class UnitEntity extends TenantAwareEntity {

    @Column(name = "unit_number", nullable = false, length = 50)
    private String unitNumber;

    @Column(name = "block_name", length = 50)
    private String block;

    @Column(name = "unit_type", length = 50)
    private String type;

    @Column(name = "square_feet", precision = 12, scale = 2)
    private BigDecimal squareFeet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnitStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "ownership_status", nullable = false, length = 20)
    private UnitOwnershipStatus ownershipStatus = UnitOwnershipStatus.OWNED;
}
