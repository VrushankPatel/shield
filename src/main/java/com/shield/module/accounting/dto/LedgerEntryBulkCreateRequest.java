package com.shield.module.accounting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record LedgerEntryBulkCreateRequest(
        @NotEmpty List<@Valid LedgerEntryCreateRequest> entries) {
}
