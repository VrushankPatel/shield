package com.shield.module.payroll.dto;

import com.shield.module.payroll.entity.PayrollComponentType;
import java.math.BigDecimal;
import java.util.UUID;

public record PayrollDetailResponse(
        UUID id,
        UUID payrollComponentId,
        String componentName,
        PayrollComponentType componentType,
        BigDecimal amount,
        boolean taxable
) {
}
