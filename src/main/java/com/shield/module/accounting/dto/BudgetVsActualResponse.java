package com.shield.module.accounting.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetVsActualResponse(
        UUID accountHeadId,
        String accountHeadName,
        String financialYear,
        BigDecimal budgetedAmount,
        BigDecimal actualAmount,
        BigDecimal variance) {
}
