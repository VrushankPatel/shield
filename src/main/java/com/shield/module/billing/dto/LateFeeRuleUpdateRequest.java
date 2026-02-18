package com.shield.module.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record LateFeeRuleUpdateRequest(
        @NotBlank String ruleName,
        @NotNull Integer daysAfterDue,
        @NotBlank String feeType,
        @NotNull @Positive BigDecimal feeAmount,
        boolean active) {
}
