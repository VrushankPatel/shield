package com.shield.module.analytics.dto;

import java.math.BigDecimal;

public record FundAllocationResponse(
        String category,
        BigDecimal amount
) {
}
