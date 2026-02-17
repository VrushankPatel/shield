package com.shield.module.helpdesk.dto;

import java.time.Instant;
import java.util.UUID;

public record HelpdeskCommentResponse(
        UUID id,
        UUID tenantId,
        UUID ticketId,
        UUID userId,
        String comment,
        boolean internalNote,
        Instant createdAt
) {
}
