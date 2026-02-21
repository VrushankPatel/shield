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
@Table(name = "diesel_generator")
public class DieselGeneratorEntity extends TenantAwareEntity {

    @Column(name = "generator_name", nullable = false, length = 100)
    private String generatorName;

    @Column(name = "capacity_kva", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacityKva;

    @Column(length = 255)
    private String location;
}
