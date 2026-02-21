package com.shield.module.meeting.dto;

import com.shield.module.meeting.entity.MeetingActionItemStatus;
import java.time.LocalDate;
import java.util.UUID;

public record MeetingActionItemResponse(
        UUID id,
        UUID tenantId,
        UUID meetingId,
        String actionDescription,
        UUID assignedTo,
        LocalDate dueDate,
        String priority,
        MeetingActionItemStatus status,
        LocalDate completionDate
) {
}
