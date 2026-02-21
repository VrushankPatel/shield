package com.shield.module.accounting.dto;

import jakarta.validation.constraints.NotBlank;

public record VendorCreateRequest(
        @NotBlank String vendorName,
        String contactPerson,
        String phone,
        String email,
        String address,
        String gstin,
        String pan,
        String vendorType) {
}
