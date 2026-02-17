package com.shield.module.utility.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "electricity_reading")
public class ElectricityReadingEntity extends TenantAwareEntity {

    @Column(name = "meter_id", nullable = false, columnDefinition = "uuid")
    private UUID meterId;

    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;

    @Column(name = "reading_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal readingValue;

    @Column(name = "units_consumed", precision = 10, scale = 2)
    private BigDecimal unitsConsumed;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "recorded_by", columnDefinition = "uuid")
    private UUID recordedBy;
}
