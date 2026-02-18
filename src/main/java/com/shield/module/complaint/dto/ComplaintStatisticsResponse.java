package com.shield.module.complaint.dto;

public record ComplaintStatisticsResponse(
        long total,
        long open,
        long assigned,
        long inProgress,
        long resolved,
        long closed,
        long slaBreached
) {
}
