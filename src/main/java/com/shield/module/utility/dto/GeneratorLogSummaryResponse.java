package com.shield.module.utility.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GeneratorLogSummaryResponse(
        UUID generatorId,
        LocalDate from,
        LocalDate to,
        int totalLogs,
        BigDecimal totalRuntimeHours,
        BigDecimal totalDieselConsumed,
        BigDecimal totalDieselCost,
        BigDecimal totalUnitsGenerated
) {
}
