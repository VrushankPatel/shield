package com.shield.module.poll.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PollVoteRequest(
        @NotNull UUID optionId) {
}
