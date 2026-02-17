package com.shield.module.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record MeetingCreateRequest(
        @NotBlank String title,
        String agenda,
        @NotNull Instant scheduledAt
) {
}
