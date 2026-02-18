package com.shield.module.accounting.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record FinancialReportResponse(
        String reportType,
        List<FinancialReportLine> lines,
        BigDecimal total,
        Instant generatedAt) {
}
