package com.shield.module.helpdesk.dto;

import com.shield.module.helpdesk.entity.TicketPriority;
import com.shield.module.helpdesk.entity.TicketStatus;
import java.time.Instant;
import java.util.UUID;

public record HelpdeskTicketResponse(
        UUID id,
        UUID tenantId,
        String ticketNumber,
        UUID categoryId,
        UUID raisedBy,
        UUID unitId,
        String subject,
        String description,
        TicketPriority priority,
        TicketStatus status,
        UUID assignedTo,
        Instant assignedAt,
        Instant resolvedAt,
        String resolutionNotes
) {
}
