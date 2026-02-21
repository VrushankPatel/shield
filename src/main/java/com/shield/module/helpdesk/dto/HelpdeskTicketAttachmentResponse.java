package com.shield.module.helpdesk.dto;

import java.time.Instant;
import java.util.UUID;

public record HelpdeskTicketAttachmentResponse(
        UUID id,
        UUID tenantId,
        UUID ticketId,
        String fileName,
        String fileUrl,
        UUID uploadedBy,
        Instant uploadedAt
) {
}
