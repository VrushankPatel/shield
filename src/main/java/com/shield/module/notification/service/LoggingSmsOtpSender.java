package com.shield.module.notification.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingSmsOtpSender implements SmsOtpSender {

    private final Map<String, String> lastOtpByPhone = new ConcurrentHashMap<>();

    @Override
    public void sendLoginOtp(String phoneNumber, String otpCode, Instant expiresAt) {
        lastOtpByPhone.put(phoneNumber, otpCode);
        log.info("Dummy SMS OTP dispatched to {} with expiry {}", maskPhone(phoneNumber), expiresAt);
    }

    public Optional<String> getLastOtp(String phoneNumber) {
        return Optional.ofNullable(lastOtpByPhone.get(phoneNumber));
    }

    private String maskPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return "*".repeat(Math.max(0, phoneNumber.length() - 4)) + phoneNumber.substring(phoneNumber.length() - 4);
    }
}
