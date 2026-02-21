package com.shield.module.payroll.dto;

import com.shield.module.payroll.entity.PayrollComponentType;
import java.util.UUID;

public record PayrollComponentResponse(
        UUID id,
        UUID tenantId,
        String componentName,
        PayrollComponentType componentType,
        boolean taxable
) {
}
