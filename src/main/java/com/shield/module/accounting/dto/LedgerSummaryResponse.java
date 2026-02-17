package com.shield.module.accounting.dto;

import java.math.BigDecimal;

public record LedgerSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance
) {
}
