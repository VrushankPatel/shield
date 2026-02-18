package com.shield.module.poll.dto;

import java.util.List;
import java.util.UUID;

public record PollResultsResponse(
        UUID pollId,
        String title,
        long totalVotes,
        List<PollOptionResult> results) {
}
