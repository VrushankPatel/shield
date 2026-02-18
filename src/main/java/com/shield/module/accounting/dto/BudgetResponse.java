package com.shield.module.accounting.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        UUID tenantId,
        String financialYear,
        UUID accountHeadId,
        BigDecimal budgetedAmount) {
}
