package com.shield.module.emergency.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "safety_inspection")
public class SafetyInspectionEntity extends TenantAwareEntity {

    @Column(name = "equipment_id", nullable = false, columnDefinition = "uuid")
    private UUID equipmentId;

    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;

    @Column(name = "inspected_by", columnDefinition = "uuid")
    private UUID inspectedBy;

    @Column(name = "inspection_result", nullable = false, length = 50)
    private String inspectionResult;

    @Column(length = 2000)
    private String remarks;
}
