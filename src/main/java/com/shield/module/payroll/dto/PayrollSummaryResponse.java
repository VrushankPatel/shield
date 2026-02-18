package com.shield.module.payroll.dto;

import java.math.BigDecimal;

public record PayrollSummaryResponse(
        Integer year,
        Integer month,
        long totalPayrolls,
        BigDecimal grossAmount,
        BigDecimal totalDeductions,
        BigDecimal netAmount
) {
}
