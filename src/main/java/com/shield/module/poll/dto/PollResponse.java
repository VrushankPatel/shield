package com.shield.module.poll.dto;

import com.shield.module.poll.entity.PollStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PollResponse(
        UUID id,
        UUID tenantId,
        String title,
        String description,
        PollStatus status,
        boolean multipleChoice,
        Instant expiresAt,
        UUID createdBy,
        List<PollOptionResponse> options,
        Instant createdAt,
        Instant updatedAt) {
}
