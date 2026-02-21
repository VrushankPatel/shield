package com.shield.module.meeting.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MeetingAttendeeCreateRequest(@NotNull UUID userId) {
}
