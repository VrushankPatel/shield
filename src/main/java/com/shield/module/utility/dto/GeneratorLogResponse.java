package com.shield.module.utility.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record GeneratorLogResponse(
        UUID id,
        UUID tenantId,
        UUID generatorId,
        LocalDate logDate,
        Instant startTime,
        Instant stopTime,
        BigDecimal runtimeHours,
        BigDecimal dieselConsumed,
        BigDecimal dieselCost,
        BigDecimal meterReadingBefore,
        BigDecimal meterReadingAfter,
        BigDecimal unitsGenerated,
        UUID operatorId
) {
}
