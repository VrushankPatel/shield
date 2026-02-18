package com.shield.module.meeting.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MeetingAgendaOrderRequest(
        @NotNull @Min(1) Integer displayOrder
) {
}
