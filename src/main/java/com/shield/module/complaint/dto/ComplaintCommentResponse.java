package com.shield.module.complaint.dto;

import java.time.Instant;
import java.util.UUID;

public record ComplaintCommentResponse(
        UUID id,
        UUID tenantId,
        UUID complaintId,
        UUID userId,
        String comment,
        Instant createdAt
) {
}
