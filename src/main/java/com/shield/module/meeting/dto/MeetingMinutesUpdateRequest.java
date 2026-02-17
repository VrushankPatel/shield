package com.shield.module.meeting.dto;

import jakarta.validation.constraints.NotBlank;

public record MeetingMinutesUpdateRequest(@NotBlank String minutes) {
}
