package com.shield.module.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record MeetingResolutionCreateRequest(
        @NotBlank String resolutionText,
        UUID proposedBy,
        UUID secondedBy
) {
}
