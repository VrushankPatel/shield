package com.shield.module.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PayrollBulkProcessRequest(
        @NotEmpty List<UUID> payrollIds,
        @NotBlank @Size(max = 50) String paymentMethod,
        @Size(max = 255) String paymentReferencePrefix,
        LocalDate paymentDate,
        @Size(max = 2000) String payslipBaseUrl
) {
}
