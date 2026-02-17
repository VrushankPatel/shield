package com.shield.module.emergency.dto;

import java.util.UUID;

public record EmergencyContactResponse(
        UUID id,
        UUID tenantId,
        String contactType,
        String contactName,
        String phonePrimary,
        String phoneSecondary,
        String address,
        Integer displayOrder,
        boolean active
) {
}
