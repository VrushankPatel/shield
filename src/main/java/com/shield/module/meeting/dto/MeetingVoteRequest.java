package com.shield.module.meeting.dto;

import com.shield.module.meeting.entity.MeetingVoteChoice;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MeetingVoteRequest(
        UUID userId,
        @NotNull MeetingVoteChoice vote
) {
}
