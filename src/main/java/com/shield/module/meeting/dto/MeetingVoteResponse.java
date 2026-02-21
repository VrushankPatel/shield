package com.shield.module.meeting.dto;

import com.shield.module.meeting.entity.MeetingVoteChoice;
import java.time.Instant;
import java.util.UUID;

public record MeetingVoteResponse(
        UUID id,
        UUID tenantId,
        UUID resolutionId,
        UUID userId,
        MeetingVoteChoice vote,
        Instant votedAt
) {
}
