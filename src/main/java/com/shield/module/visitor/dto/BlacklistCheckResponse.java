package com.shield.module.visitor.dto;

public record BlacklistCheckResponse(
        String phone,
        boolean blacklisted
) {
}
