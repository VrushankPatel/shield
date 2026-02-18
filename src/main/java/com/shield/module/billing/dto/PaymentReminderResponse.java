package com.shield.module.billing.dto;

import com.shield.module.billing.entity.ReminderStatus;
import java.time.Instant;
import java.util.UUID;

public record PaymentReminderResponse(
        UUID id,
        UUID tenantId,
        UUID invoiceId,
        String reminderType,
        Instant sentAt,
        String channel,
        ReminderStatus status) {
}
