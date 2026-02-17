package com.shield.module.complaint.dto;

import com.shield.module.complaint.entity.ComplaintPriority;
import com.shield.module.complaint.entity.ComplaintStatus;
import java.time.Instant;
import java.util.UUID;

public record ComplaintResponse(
        UUID id,
        UUID tenantId,
        UUID assetId,
        UUID unitId,
        String title,
        String description,
        ComplaintPriority priority,
        ComplaintStatus status,
        UUID assignedTo,
        Instant resolvedAt
) {
}
