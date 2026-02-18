package com.shield.module.notification.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record NotificationBulkSendRequest(
        @NotEmpty List<@Valid NotificationSendRequest> notifications) {
}
