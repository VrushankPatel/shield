package com.shield.module.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record NotificationSendRequest(
        @NotEmpty List<@Email @Size(max = 255) String> recipients,
        @NotBlank @Size(max = 255) String subject,
        @NotBlank @Size(max = 20000) String body
) {
}
