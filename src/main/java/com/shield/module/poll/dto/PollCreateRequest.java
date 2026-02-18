package com.shield.module.poll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public record PollCreateRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 20000) String description,
        boolean multipleChoice,
        Instant expiresAt,
        @NotEmpty List<@NotBlank @Size(max = 500) String> options) {
}
