package com.shield.module.complaint.dto;

import com.shield.module.complaint.entity.WorkOrderStatus;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record WorkOrderUpdateRequest(
        UUID assetId,
        UUID vendorId,
        @NotBlank String workDescription,
        BigDecimal estimatedCost,
        BigDecimal actualCost,
        LocalDate scheduledDate,
        LocalDate completionDate,
        WorkOrderStatus status
) {
}
