package com.shield.module.emergency.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sos_alert")
public class SosAlertEntity extends TenantAwareEntity {

    @Column(name = "alert_number", nullable = false, length = 100)
    private String alertNumber;

    @Column(name = "raised_by", columnDefinition = "uuid")
    private UUID raisedBy;

    @Column(name = "unit_id", columnDefinition = "uuid")
    private UUID unitId;

    @Column(name = "alert_type", nullable = false, length = 100)
    private String alertType;

    @Column(length = 255)
    private String location;

    @Column(length = 2000)
    private String description;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SosAlertStatus status;

    @Column(name = "responded_by", columnDefinition = "uuid")
    private UUID respondedBy;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
