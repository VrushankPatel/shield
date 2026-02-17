package com.shield.module.utility.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "water_tank")
public class WaterTankEntity extends TenantAwareEntity {

    @Column(name = "tank_name", nullable = false, length = 100)
    private String tankName;

    @Column(name = "tank_type", nullable = false, length = 50)
    private String tankType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal capacity;

    @Column(length = 255)
    private String location;
}
