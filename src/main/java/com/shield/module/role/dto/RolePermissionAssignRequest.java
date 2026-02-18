package com.shield.module.role.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record RolePermissionAssignRequest(
        @NotEmpty List<UUID> permissionIds
) {
}
