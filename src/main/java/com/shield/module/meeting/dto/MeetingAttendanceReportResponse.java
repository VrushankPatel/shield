package com.shield.module.meeting.dto;

import java.util.UUID;

public record MeetingAttendanceReportResponse(
        UUID meetingId,
        long totalAttendees,
        long accepted,
        long declined,
        long pending,
        long present,
        long absent
) {
}
