package com.shield.module.notification.dto;

public record NotificationDispatchResponse(
        int total,
        int sent,
        int failed,
        int skipped
) {
}
