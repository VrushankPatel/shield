package com.shield.module.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PaymentReminderSendRequest(
        @NotNull UUID invoiceId,
        @NotBlank String reminderType,
        @NotBlank String channel) {
}
