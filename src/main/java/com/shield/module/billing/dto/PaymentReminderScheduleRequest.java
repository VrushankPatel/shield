package com.shield.module.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record PaymentReminderScheduleRequest(
        @NotNull UUID invoiceId,
        @NotBlank String reminderType,
        @NotBlank String channel,
        @NotNull Instant scheduledAt) {
}
