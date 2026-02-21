package com.shield.module.payroll.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "staff_salary_structure")
public class StaffSalaryStructureEntity extends TenantAwareEntity {

    @Column(name = "staff_id", nullable = false, columnDefinition = "uuid")
    private UUID staffId;

    @Column(name = "payroll_component_id", nullable = false, columnDefinition = "uuid")
    private UUID payrollComponentId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
}
