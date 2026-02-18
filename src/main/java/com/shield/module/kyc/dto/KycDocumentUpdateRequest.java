package com.shield.module.kyc.dto;

import com.shield.module.kyc.entity.KycDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record KycDocumentUpdateRequest(
        @NotNull KycDocumentType documentType,
        @NotBlank @Size(max = 100) String documentNumber,
        @Size(max = 2000) String documentUrl
) {
}
