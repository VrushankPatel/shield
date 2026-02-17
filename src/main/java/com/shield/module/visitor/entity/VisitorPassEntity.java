package com.shield.module.visitor.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "visitor_pass")
public class VisitorPassEntity extends TenantAwareEntity {

    @Column(name = "unit_id", nullable = false, columnDefinition = "uuid")
    private UUID unitId;

    @Column(name = "visitor_name", nullable = false, length = 200)
    private String visitorName;

    @Column(name = "vehicle_number", length = 40)
    private String vehicleNumber;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to", nullable = false)
    private Instant validTo;

    @Column(name = "qr_code", length = 500)
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VisitorPassStatus status;
}
