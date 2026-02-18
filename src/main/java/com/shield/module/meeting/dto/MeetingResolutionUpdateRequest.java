package com.shield.module.meeting.dto;

import com.shield.module.meeting.entity.MeetingResolutionStatus;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record MeetingResolutionUpdateRequest(
        @NotBlank String resolutionText,
        UUID proposedBy,
        UUID secondedBy,
        MeetingResolutionStatus status
) {
}
