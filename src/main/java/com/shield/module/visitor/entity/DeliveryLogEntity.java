package com.shield.module.visitor.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "delivery_log")
public class DeliveryLogEntity extends TenantAwareEntity {

    @Column(name = "unit_id", nullable = false, columnDefinition = "uuid")
    private UUID unitId;

    @Column(name = "delivery_partner", nullable = false, length = 100)
    private String deliveryPartner;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "delivery_time", nullable = false)
    private Instant deliveryTime;

    @Column(name = "received_by", columnDefinition = "uuid")
    private UUID receivedBy;

    @Column(name = "security_guard_id", columnDefinition = "uuid")
    private UUID securityGuardId;

    @Column(name = "photo_url", length = 1000)
    private String photoUrl;
}
