package com.shield.module.poll.dto;

import java.time.Instant;
import java.util.UUID;

public record PollOptionResponse(
        UUID id,
        String optionText,
        int displayOrder,
        Instant createdAt) {
}
