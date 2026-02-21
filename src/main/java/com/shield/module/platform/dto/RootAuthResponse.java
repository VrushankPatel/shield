package com.shield.module.platform.dto;

public record RootAuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        boolean passwordChangeRequired
) {
}
