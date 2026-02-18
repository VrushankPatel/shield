package com.shield.module.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record MeetingMinutesCreateRequest(
        @NotBlank String minutesContent,
        String summary,
        UUID preparedBy,
        String documentUrl
) {
}
