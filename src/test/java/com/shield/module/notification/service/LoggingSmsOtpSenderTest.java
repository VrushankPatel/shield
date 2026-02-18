package com.shield.module.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class LoggingSmsOtpSenderTest {

    @Test
    void shouldStoreLastOtpPerPhone() {
        LoggingSmsOtpSender sender = new LoggingSmsOtpSender();
        sender.sendLoginOtp("9999999999", "123456", Instant.parse("2026-02-18T05:00:00Z"));

        assertTrue(sender.getLastOtp("9999999999").isPresent());
        assertEquals("123456", sender.getLastOtp("9999999999").orElseThrow());
    }
}
