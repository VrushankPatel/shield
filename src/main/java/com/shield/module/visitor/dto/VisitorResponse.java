package com.shield.module.visitor.dto;

import java.util.UUID;

public record VisitorResponse(
        UUID id,
        UUID tenantId,
        String visitorName,
        String phone,
        String vehicleNumber,
        String visitorType,
        String idProofType,
        String idProofNumber,
        String photoUrl
) {
}
