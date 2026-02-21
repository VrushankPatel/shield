package com.shield.module.complaint.dto;

import com.shield.module.complaint.entity.ComplaintPriority;
import com.shield.module.complaint.entity.ComplaintStatus;
import java.time.Instant;
import java.util.UUID;

public record ComplaintResponse(
        UUID id,
        UUID tenantId,
        String complaintNumber,
        UUID assetId,
        UUID raisedBy,
        UUID unitId,
        String title,
        String description,
        String complaintType,
        String location,
        ComplaintPriority priority,
        ComplaintStatus status,
        UUID assignedTo,
        Instant assignedAt,
        Instant resolvedAt,
        String resolutionNotes,
        Instant closedAt,
        Integer slaHours,
        boolean slaBreach,
        Instant createdAt
) {
}
