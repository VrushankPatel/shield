package com.shield.module.analytics.dto;

import java.math.BigDecimal;

public record DefaulterTrendResponse(
        String period,
        long defaulterUnits,
        BigDecimal outstandingAmount
) {
}
