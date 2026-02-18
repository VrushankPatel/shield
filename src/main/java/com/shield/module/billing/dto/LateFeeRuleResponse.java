package com.shield.module.billing.dto;

import com.shield.module.billing.entity.LateFeeType;
import java.math.BigDecimal;
import java.util.UUID;

public record LateFeeRuleResponse(
        UUID id,
        UUID tenantId,
        String ruleName,
        Integer daysAfterDue,
        LateFeeType feeType,
        BigDecimal feeAmount,
        boolean active) {
}
