package com.shield.module.asset.dto;

import com.shield.module.asset.entity.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AssetCreateRequest(
        @NotBlank String assetCode,
        @NotBlank String category,
        String location,
        @NotNull AssetStatus status,
        LocalDate purchaseDate
) {
}
