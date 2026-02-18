package com.shield.module.notification.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class LoggingWhatsappNotificationSenderTest {

    @Test
    void shouldLogWithoutThrowing() {
        LoggingWhatsappNotificationSender sender = new LoggingWhatsappNotificationSender();
        assertDoesNotThrow(() -> sender.sendMessage("+919999999999", "Sample message"));
    }
}
