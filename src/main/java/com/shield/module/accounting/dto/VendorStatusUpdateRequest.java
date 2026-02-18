package com.shield.module.accounting.dto;

import jakarta.validation.constraints.NotNull;

public record VendorStatusUpdateRequest(@NotNull Boolean active) {
}
