package com.shield.module.role.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UserRoleAssignRequest(
        @NotNull UUID roleId
) {
}
