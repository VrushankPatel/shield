package com.shield.module.kyc.dto;

import com.shield.module.kyc.entity.KycDocumentType;
import com.shield.module.kyc.entity.KycVerificationStatus;
import java.time.Instant;
import java.util.UUID;

public record KycDocumentResponse(
        UUID id,
        UUID tenantId,
        UUID userId,
        KycDocumentType documentType,
        String documentNumber,
        String documentUrl,
        KycVerificationStatus verificationStatus,
        String rejectionReason,
        Instant verifiedAt,
        UUID verifiedBy,
        Instant createdAt
) {
}
