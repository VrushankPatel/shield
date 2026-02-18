package com.shield.module.complaint.dto;

import com.shield.module.complaint.entity.WorkOrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record WorkOrderResponse(
        UUID id,
        UUID tenantId,
        String workOrderNumber,
        UUID complaintId,
        UUID assetId,
        UUID vendorId,
        String workDescription,
        BigDecimal estimatedCost,
        BigDecimal actualCost,
        LocalDate scheduledDate,
        LocalDate completionDate,
        WorkOrderStatus status,
        UUID createdBy
) {
}
