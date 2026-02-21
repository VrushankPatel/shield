package com.shield.module.meeting.dto;

import com.shield.module.meeting.entity.MeetingAttendeeRsvpStatus;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MeetingAttendeeRsvpRequest(
        UUID userId,
        @NotNull MeetingAttendeeRsvpStatus rsvpStatus
) {
}
