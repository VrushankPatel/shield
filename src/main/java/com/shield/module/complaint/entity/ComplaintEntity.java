package com.shield.module.complaint.entity;

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
@Table(name = "complaint")
public class ComplaintEntity extends TenantAwareEntity {

    @Column(name = "asset_id", columnDefinition = "uuid")
    private UUID assetId;

    @Column(name = "unit_id", nullable = false, columnDefinition = "uuid")
    private UUID unitId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplaintPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplaintStatus status;

    @Column(name = "assigned_to", columnDefinition = "uuid")
    private UUID assignedTo;

    @Column(name = "resolved_at")
    private Instant resolvedAt;
}
