package com.shield.module.user.dto;

import com.shield.module.user.entity.UserRole;
import com.shield.module.user.entity.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        UUID tenantId,
        UUID unitId,
        String name,
        String email,
        String phone,
        UserRole role,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
