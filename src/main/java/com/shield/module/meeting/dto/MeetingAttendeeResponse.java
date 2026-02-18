package com.shield.module.meeting.dto;

import com.shield.module.meeting.entity.MeetingAttendeeAttendanceStatus;
import com.shield.module.meeting.entity.MeetingAttendeeRsvpStatus;
import java.time.Instant;
import java.util.UUID;

public record MeetingAttendeeResponse(
        UUID id,
        UUID tenantId,
        UUID meetingId,
        UUID userId,
        Instant invitationSentAt,
        MeetingAttendeeRsvpStatus rsvpStatus,
        MeetingAttendeeAttendanceStatus attendanceStatus,
        Instant joinedAt,
        Instant leftAt
) {
}
