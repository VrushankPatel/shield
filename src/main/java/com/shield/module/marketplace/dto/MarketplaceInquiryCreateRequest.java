package com.shield.module.marketplace.dto;

import jakarta.validation.constraints.Size;

public record MarketplaceInquiryCreateRequest(
        @Size(max = 2000) String message
) {
}
