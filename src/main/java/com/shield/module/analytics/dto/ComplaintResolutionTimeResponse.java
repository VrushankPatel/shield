package com.shield.module.analytics.dto;

import java.math.BigDecimal;

public record ComplaintResolutionTimeResponse(
        long resolvedCount,
        BigDecimal averageResolutionHours
) {
}
