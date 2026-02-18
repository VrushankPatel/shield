package com.shield.module.complaint.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "work_order")
public class WorkOrderEntity extends TenantAwareEntity {

    @Column(name = "work_order_number", nullable = false, length = 100)
    private String workOrderNumber;

    @Column(name = "complaint_id", nullable = false, columnDefinition = "uuid")
    private UUID complaintId;

    @Column(name = "asset_id", columnDefinition = "uuid")
    private UUID assetId;

    @Column(name = "vendor_id", columnDefinition = "uuid")
    private UUID vendorId;

    @Column(name = "work_description", nullable = false, length = 2000)
    private String workDescription;

    @Column(name = "estimated_cost", precision = 12, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "actual_cost", precision = 12, scale = 2)
    private BigDecimal actualCost;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private WorkOrderStatus status;

    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdBy;
}
