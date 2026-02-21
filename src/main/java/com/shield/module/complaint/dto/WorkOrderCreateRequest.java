package com.shield.module.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record WorkOrderCreateRequest(
        @NotNull UUID complaintId,
        UUID assetId,
        UUID vendorId,
        @NotBlank String workDescription,
        BigDecimal estimatedCost,
        LocalDate scheduledDate
) {
}
