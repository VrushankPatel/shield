package com.shield.module.analytics.dto;

import java.math.BigDecimal;

public record OccupancyRateResponse(
        long totalUnits,
        long occupiedUnits,
        BigDecimal occupancyRatePercent
) {
}
