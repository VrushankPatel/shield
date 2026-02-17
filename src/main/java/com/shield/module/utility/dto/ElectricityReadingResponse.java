package com.shield.module.utility.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ElectricityReadingResponse(
        UUID id,
        UUID tenantId,
        UUID meterId,
        LocalDate readingDate,
        BigDecimal readingValue,
        BigDecimal unitsConsumed,
        BigDecimal cost,
        UUID recordedBy
) {
}
