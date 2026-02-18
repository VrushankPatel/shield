package com.shield.module.billing.repository;

import com.shield.module.billing.entity.PaymentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByIdAndDeletedFalse(UUID id);

    Optional<PaymentEntity> findByTransactionRefAndDeletedFalse(String transactionRef);
}
