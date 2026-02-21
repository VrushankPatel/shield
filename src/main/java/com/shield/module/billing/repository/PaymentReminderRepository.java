package com.shield.module.billing.repository;

import com.shield.module.billing.entity.PaymentReminderEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentReminderRepository extends JpaRepository<PaymentReminderEntity, UUID> {

    Optional<PaymentReminderEntity> findByIdAndDeletedFalse(UUID id);

    Page<PaymentReminderEntity> findAllByDeletedFalse(Pageable pageable);

    List<PaymentReminderEntity> findAllByInvoiceIdAndDeletedFalse(UUID invoiceId);
}
