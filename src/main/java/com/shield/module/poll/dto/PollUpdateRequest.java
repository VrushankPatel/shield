package com.shield.module.poll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record PollUpdateRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 20000) String description,
        Instant expiresAt) {
}
