package com.shield.module.accounting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record BudgetCreateRequest(
        @NotBlank String financialYear,
        @NotNull UUID accountHeadId,
        @NotNull @Positive BigDecimal budgetedAmount) {
}
