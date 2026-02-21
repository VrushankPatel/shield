package com.shield.module.utility.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WaterLevelChartDataResponse(
        UUID tankId,
        Instant from,
        Instant to,
        int totalPoints,
        List<WaterLevelChartPointResponse> points
) {
}
