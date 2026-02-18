package com.shield.module.visitor.entity;

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
@Table(name = "domestic_help_unit_mapping")
public class DomesticHelpUnitMappingEntity extends TenantAwareEntity {

    @Column(name = "domestic_help_id", nullable = false, columnDefinition = "uuid")
    private UUID domesticHelpId;

    @Column(name = "unit_id", nullable = false, columnDefinition = "uuid")
    private UUID unitId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
