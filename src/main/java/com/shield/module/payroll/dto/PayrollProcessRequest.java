package com.shield.module.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record PayrollProcessRequest(
        @NotNull UUID payrollId,
        @NotBlank @Size(max = 50) String paymentMethod,
        @Size(max = 255) String paymentReference,
        LocalDate paymentDate,
        @Size(max = 2000) String payslipUrl
) {
}
