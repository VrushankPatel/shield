package com.shield.module.notification.service;

import java.time.Instant;

public interface SmsOtpSender {

    void sendLoginOtp(String phoneNumber, String otpCode, Instant expiresAt);
}
