package com.shield.module.meeting.dto;

import com.shield.module.meeting.entity.MeetingStatus;
import java.time.Instant;
import java.util.UUID;

public record MeetingResponse(
        UUID id,
        UUID tenantId,
        String meetingNumber,
        String meetingType,
        String title,
        String agenda,
        Instant scheduledAt,
        String location,
        String meetingMode,
        String meetingLink,
        UUID createdBy,
        String minutes,
        MeetingStatus status
) {
}
