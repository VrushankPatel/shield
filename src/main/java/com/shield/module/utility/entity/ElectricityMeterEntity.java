package com.shield.module.utility.entity;

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
@Table(name = "electricity_meter")
public class ElectricityMeterEntity extends TenantAwareEntity {

    @Column(name = "meter_number", nullable = false, length = 100)
    private String meterNumber;

    @Column(name = "meter_type", nullable = false, length = 50)
    private String meterType;

    @Column(length = 255)
    private String location;

    @Column(name = "unit_id", columnDefinition = "uuid")
    private UUID unitId;
}
