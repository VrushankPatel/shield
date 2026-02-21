package com.shield.module.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record MeetingCreateRequest(
        String meetingType,
        @NotBlank String title,
        String agenda,
        @NotNull Instant scheduledAt,
        String location,
        String meetingMode,
        String meetingLink
) {
}
