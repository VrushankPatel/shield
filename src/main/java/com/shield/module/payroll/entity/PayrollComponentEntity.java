package com.shield.module.payroll.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payroll_component")
public class PayrollComponentEntity extends TenantAwareEntity {

    @Column(name = "component_name", nullable = false, length = 100)
    private String componentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false, length = 50)
    private PayrollComponentType componentType;

    @Column(nullable = false)
    private boolean taxable = true;
}
