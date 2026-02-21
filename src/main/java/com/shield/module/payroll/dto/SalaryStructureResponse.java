package com.shield.module.payroll.dto;

import com.shield.module.payroll.entity.PayrollComponentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SalaryStructureResponse(
        UUID id,
        UUID tenantId,
        UUID staffId,
        UUID payrollComponentId,
        String payrollComponentName,
        PayrollComponentType payrollComponentType,
        BigDecimal amount,
        boolean active,
        LocalDate effectiveFrom
) {
}
