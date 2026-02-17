package com.shield.module.utility.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "water_level_log")
public class WaterLevelLogEntity extends TenantAwareEntity {

    @Column(name = "tank_id", nullable = false, columnDefinition = "uuid")
    private UUID tankId;

    @Column(name = "reading_time", nullable = false)
    private Instant readingTime;

    @Column(name = "level_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal levelPercentage;

    @Column(precision = 10, scale = 2)
    private BigDecimal volume;

    @Column(name = "recorded_by", columnDefinition = "uuid")
    private UUID recordedBy;
}
