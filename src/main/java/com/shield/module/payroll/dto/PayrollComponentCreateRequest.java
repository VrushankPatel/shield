package com.shield.module.payroll.dto;

import com.shield.module.payroll.entity.PayrollComponentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PayrollComponentCreateRequest(
        @NotBlank @Size(max = 100) String componentName,
        @NotNull PayrollComponentType componentType,
        boolean taxable
) {
}
