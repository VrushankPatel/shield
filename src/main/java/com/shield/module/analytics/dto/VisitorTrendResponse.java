package com.shield.module.analytics.dto;

import java.time.LocalDate;

public record VisitorTrendResponse(
        LocalDate visitDate,
        long visitorCount
) {
}
