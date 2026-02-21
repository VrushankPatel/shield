package com.shield.module.payroll.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payroll_detail")
public class PayrollDetailEntity extends TenantAwareEntity {

    @Column(name = "payroll_id", nullable = false, columnDefinition = "uuid")
    private UUID payrollId;

    @Column(name = "payroll_component_id", nullable = false, columnDefinition = "uuid")
    private UUID payrollComponentId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
}
