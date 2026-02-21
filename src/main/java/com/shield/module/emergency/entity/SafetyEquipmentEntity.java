package com.shield.module.emergency.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "safety_equipment")
public class SafetyEquipmentEntity extends TenantAwareEntity {

    @Column(name = "equipment_type", nullable = false, length = 100)
    private String equipmentType;

    @Column(name = "equipment_tag", length = 100)
    private String equipmentTag;

    @Column(length = 255)
    private String location;

    @Column(name = "installation_date")
    private LocalDate installationDate;

    @Column(name = "last_inspection_date")
    private LocalDate lastInspectionDate;

    @Column(name = "next_inspection_date")
    private LocalDate nextInspectionDate;

    @Column(name = "inspection_frequency_days")
    private Integer inspectionFrequencyDays;

    @Column(name = "functional", nullable = false)
    private boolean functional;
}
