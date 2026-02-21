package com.shield.module.accounting.dto;

import java.util.UUID;

public record VendorResponse(
        UUID id,
        UUID tenantId,
        String vendorName,
        String contactPerson,
        String phone,
        String email,
        String address,
        String gstin,
        String pan,
        String vendorType,
        boolean active) {
}
