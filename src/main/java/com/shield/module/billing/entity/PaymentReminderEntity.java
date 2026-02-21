package com.shield.module.billing.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payment_reminder")
public class PaymentReminderEntity extends TenantAwareEntity {

    @Column(name = "invoice_id", nullable = false, columnDefinition = "uuid")
    private UUID invoiceId;

    @Column(name = "reminder_type", nullable = false, length = 50)
    private String reminderType;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(nullable = false, length = 50)
    private String channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReminderStatus status;
}
