package com.shield.module.meeting.dto;

import com.shield.module.meeting.entity.MeetingResolutionStatus;
import java.util.UUID;

public record MeetingResolutionResponse(
        UUID id,
        UUID tenantId,
        UUID meetingId,
        String resolutionNumber,
        String resolutionText,
        UUID proposedBy,
        UUID secondedBy,
        MeetingResolutionStatus status,
        Integer votesFor,
        Integer votesAgainst,
        Integer votesAbstain
) {
}
