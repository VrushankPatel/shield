package com.shield.module.utility.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record WaterLevelChartPointResponse(
        Instant readingTime,
        BigDecimal levelPercentage,
        BigDecimal volume
) {
}
