package com.shield.module.analytics.dto;

import java.math.BigDecimal;

public record CollectionEfficiencyResponse(
        BigDecimal billedAmount,
        BigDecimal collectedAmount,
        BigDecimal collectionEfficiencyPercent
) {
}
