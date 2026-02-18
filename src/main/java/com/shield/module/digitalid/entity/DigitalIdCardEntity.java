package com.shield.module.digitalid.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "digital_id_card")
public class DigitalIdCardEntity extends TenantAwareEntity {

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "qr_code_data", nullable = false, length = 1000)
    private String qrCodeData;

    @Column(name = "qr_code_url", length = 2000)
    private String qrCodeUrl;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "deactivated_at")
    private Instant deactivatedAt;
}
