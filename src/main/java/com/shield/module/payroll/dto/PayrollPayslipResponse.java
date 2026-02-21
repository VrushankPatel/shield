package com.shield.module.payroll.dto;

import com.shield.module.payroll.entity.PayrollStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PayrollPayslipResponse(
        UUID payrollId,
        UUID staffId,
        int month,
        int year,
        BigDecimal grossSalary,
        BigDecimal totalDeductions,
        BigDecimal manualDeductions,
        BigDecimal netSalary,
        LocalDate paymentDate,
        PayrollStatus status,
        String payslipUrl,
        Instant generatedAt,
        List<PayrollDetailResponse> earnings,
        List<PayrollDetailResponse> deductions
) {
}
