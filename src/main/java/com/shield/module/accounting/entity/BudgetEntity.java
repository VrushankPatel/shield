package com.shield.module.accounting.entity;

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
@Table(name = "budget")
public class BudgetEntity extends TenantAwareEntity {

    @Column(name = "financial_year", nullable = false, length = 20)
    private String financialYear;

    @Column(name = "account_head_id", nullable = false, columnDefinition = "uuid")
    private UUID accountHeadId;

    @Column(name = "budgeted_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal budgetedAmount;
}
