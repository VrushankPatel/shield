package com.shield.module.analytics.dto;

public record AssetFailureFrequencyResponse(
        String assetCode,
        long complaintCount
) {
}
