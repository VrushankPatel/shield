package com.shield.module.meeting.dto;

import com.shield.module.meeting.entity.MeetingAttendeeAttendanceStatus;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record MeetingAttendanceMarkRequest(
        @NotNull UUID userId,
        @NotNull MeetingAttendeeAttendanceStatus attendanceStatus,
        Instant joinedAt,
        Instant leftAt
) {
}
