package com.shield.module.meeting.dto;

import com.shield.module.meeting.entity.MeetingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record MeetingUpdateRequest(
        String meetingType,
        @NotBlank String title,
        String agenda,
        @NotNull Instant scheduledAt,
        String location,
        String meetingMode,
        String meetingLink,
        MeetingStatus status
) {
}
