package com.shield.module.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UserBulkImportRequest(
        @NotEmpty List<@Valid UserCreateRequest> users
) {
}
