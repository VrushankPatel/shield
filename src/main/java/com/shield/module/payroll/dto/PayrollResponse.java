package com.shield.module.payroll.dto;

import com.shield.module.payroll.entity.PayrollStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PayrollResponse(
        UUID id,
        UUID tenantId,
        UUID staffId,
        int month,
        int year,
        int workingDays,
        int presentDays,
        BigDecimal grossSalary,
        BigDecimal totalDeductions,
        BigDecimal netSalary,
        LocalDate paymentDate,
        String paymentMethod,
        String paymentReference,
        PayrollStatus status,
        String payslipUrl
) {
}
