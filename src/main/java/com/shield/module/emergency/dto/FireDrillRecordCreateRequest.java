package com.shield.module.emergency.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record FireDrillRecordCreateRequest(
        @NotNull LocalDate drillDate,
        LocalTime drillTime,
        UUID conductedBy,
        @Min(0) Integer evacuationTime,
        @Min(0) Integer participantsCount,
        @Size(max = 2000) String observations,
        @Size(max = 2000) String reportUrl
) {
}
