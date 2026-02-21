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

    @Column(name = "complaint_number", nullable = false, length = 100)
    private String complaintNumber;

    @Column(name = "asset_id", columnDefinition = "uuid")
    private UUID assetId;

    @Column(name = "raised_by", columnDefinition = "uuid")
    private UUID raisedBy;

    @Column(name = "unit_id", nullable = false, columnDefinition = "uuid")
    private UUID unitId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "complaint_type", length = 100)
    private String complaintType;

    @Column(length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplaintPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplaintStatus status;

    @Column(name = "assigned_to", columnDefinition = "uuid")
    private UUID assignedTo;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolution_notes", length = 2000)
    private String resolutionNotes;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "sla_hours")
    private Integer slaHours;

    @Column(name = "sla_breach", nullable = false)
    private boolean slaBreach;
}
