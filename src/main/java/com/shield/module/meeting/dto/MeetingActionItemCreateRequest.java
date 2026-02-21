package com.shield.module.meeting.dto;

import com.shield.module.meeting.entity.MeetingActionItemStatus;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.UUID;

public record MeetingActionItemCreateRequest(
        @NotBlank String actionDescription,
        UUID assignedTo,
        LocalDate dueDate,
        String priority,
        MeetingActionItemStatus status
) {
}
