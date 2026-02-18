package com.shield.module.poll.dto;

import java.util.UUID;

public record PollOptionResult(
        UUID optionId,
        String optionText,
        long voteCount,
        double percentage) {
}
