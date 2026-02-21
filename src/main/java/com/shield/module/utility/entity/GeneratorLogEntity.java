package com.shield.module.utility.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "generator_log")
public class GeneratorLogEntity extends TenantAwareEntity {

    @Column(name = "generator_id", nullable = false, columnDefinition = "uuid")
    private UUID generatorId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "stop_time")
    private Instant stopTime;

    @Column(name = "runtime_hours", precision = 5, scale = 2)
    private BigDecimal runtimeHours;

    @Column(name = "diesel_consumed", precision = 10, scale = 2)
    private BigDecimal dieselConsumed;

    @Column(name = "diesel_cost", precision = 10, scale = 2)
    private BigDecimal dieselCost;

    @Column(name = "meter_reading_before", precision = 10, scale = 2)
    private BigDecimal meterReadingBefore;

    @Column(name = "meter_reading_after", precision = 10, scale = 2)
    private BigDecimal meterReadingAfter;

    @Column(name = "units_generated", precision = 10, scale = 2)
    private BigDecimal unitsGenerated;

    @Column(name = "operator_id", columnDefinition = "uuid")
    private UUID operatorId;
}
