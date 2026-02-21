package com.shield.module.meeting.dto;

import java.time.Instant;
import java.util.UUID;

public record MeetingReminderResponse(
        UUID id,
        UUID tenantId,
        UUID meetingId,
        String reminderType,
        Instant sentAt
) {
}
