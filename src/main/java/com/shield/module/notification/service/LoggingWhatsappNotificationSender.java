package com.shield.module.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingWhatsappNotificationSender implements WhatsappNotificationSender {

    @Override
    public void sendMessage(String recipient, String message) {
        log.info("Dummy WhatsApp notification placeholder to {} with message length {}", recipient, message == null ? 0 : message.length());
    }
}
