package com.shield.module.notification.service;

public interface WhatsappNotificationSender {

    void sendMessage(String recipient, String message);
}
