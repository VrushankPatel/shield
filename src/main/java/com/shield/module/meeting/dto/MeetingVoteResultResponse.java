package com.shield.module.meeting.dto;

import com.shield.module.meeting.entity.MeetingResolutionStatus;
import java.util.UUID;

public record MeetingVoteResultResponse(
        UUID resolutionId,
        int votesFor,
        int votesAgainst,
        int votesAbstain,
        int totalVotes,
        MeetingResolutionStatus status
) {
}
