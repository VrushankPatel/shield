package com.shield.module.billing.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "billing_cycle")
public class BillingCycleEntity extends TenantAwareEntity {

    @Column(name = "cycle_name", nullable = false, length = 100)
    private String cycleName;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "late_fee_applicable_date")
    private LocalDate lateFeeApplicableDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BillingCycleStatus status;
}
