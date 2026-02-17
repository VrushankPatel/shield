package com.shield.module.utility.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WaterLevelLogResponse(
        UUID id,
        UUID tenantId,
        UUID tankId,
        Instant readingTime,
        BigDecimal levelPercentage,
        BigDecimal volume,
        UUID recordedBy
) {
}
