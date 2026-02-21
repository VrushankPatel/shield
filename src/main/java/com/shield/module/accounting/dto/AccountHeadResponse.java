package com.shield.module.accounting.dto;

import com.shield.module.accounting.entity.AccountHeadType;
import java.util.UUID;

public record AccountHeadResponse(
        UUID id,
        UUID tenantId,
        String headName,
        AccountHeadType headType,
        UUID parentHeadId) {
}
