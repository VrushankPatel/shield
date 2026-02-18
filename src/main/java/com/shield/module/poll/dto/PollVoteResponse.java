package com.shield.module.poll.dto;

import java.time.Instant;
import java.util.UUID;

public record PollVoteResponse(
        UUID id,
        UUID pollId,
        UUID optionId,
        UUID userId,
        Instant createdAt) {
}
