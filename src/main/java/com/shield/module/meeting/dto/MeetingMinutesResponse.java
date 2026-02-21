package com.shield.module.meeting.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MeetingMinutesResponse(
        UUID id,
        UUID tenantId,
        UUID meetingId,
        String minutesContent,
        String summary,
        String aiGeneratedSummary,
        UUID preparedBy,
        UUID approvedBy,
        LocalDate approvalDate,
        String documentUrl
) {
}
