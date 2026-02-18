package com.shield.module.billing.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "late_fee_rule")
public class LateFeeRuleEntity extends TenantAwareEntity {

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "days_after_due", nullable = false)
    private Integer daysAfterDue;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 30)
    private LateFeeType feeType;

    @Column(name = "fee_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal feeAmount;

    @Column(nullable = false)
    private boolean active;
}
