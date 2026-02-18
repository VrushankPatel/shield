package com.shield.module.kyc.dto;

import jakarta.validation.constraints.Size;

public record KycDecisionRequest(
        @Size(max = 500) String rejectionReason
) {
}
