package com.shield.module.utility.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ElectricityConsumptionReportResponse(
        UUID meterId,
        LocalDate fromDate,
        LocalDate toDate,
        long totalReadings,
        BigDecimal totalUnitsConsumed,
        BigDecimal totalCost
) {
}
