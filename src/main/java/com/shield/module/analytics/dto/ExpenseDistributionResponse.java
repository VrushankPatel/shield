package com.shield.module.analytics.dto;

import java.math.BigDecimal;

public record ExpenseDistributionResponse(
        String category,
        BigDecimal amount
) {
}
