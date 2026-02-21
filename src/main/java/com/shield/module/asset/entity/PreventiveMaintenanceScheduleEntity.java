package com.shield.module.asset.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "preventive_maintenance_schedule")
public class PreventiveMaintenanceScheduleEntity extends TenantAwareEntity {

    @Column(name = "asset_id", nullable = false, columnDefinition = "uuid")
    private UUID assetId;

    @Column(name = "maintenance_type", nullable = false, length = 120)
    private String maintenanceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MaintenanceFrequency frequency;

    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column(name = "next_maintenance_date", nullable = false)
    private LocalDate nextMaintenanceDate;

    @Column(name = "assigned_vendor_id", columnDefinition = "uuid")
    private UUID assignedVendorId;

    @Column(nullable = false)
    private boolean active = true;
}
