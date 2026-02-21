package com.shield.module.user.dto;

import com.shield.module.user.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequest(
        @NotNull UserStatus status
) {
}
