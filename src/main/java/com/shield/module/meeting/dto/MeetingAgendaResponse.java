package com.shield.module.meeting.dto;

import java.util.UUID;

public record MeetingAgendaResponse(
        UUID id,
        UUID tenantId,
        UUID meetingId,
        String agendaItem,
        String description,
        Integer displayOrder,
        UUID presenter,
        Integer estimatedDuration
) {
}
