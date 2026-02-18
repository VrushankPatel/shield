package com.shield.module.meeting.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MeetingAgendaUpdateRequest(
        @NotBlank String agendaItem,
        String description,
        @NotNull @Min(1) Integer displayOrder,
        UUID presenter,
        Integer estimatedDuration
) {
}
