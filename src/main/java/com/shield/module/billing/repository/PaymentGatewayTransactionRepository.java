package com.shield.module.billing.repository;

import com.shield.module.billing.entity.PaymentGatewayTransactionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentGatewayTransactionRepository extends JpaRepository<PaymentGatewayTransactionEntity, UUID> {

    Optional<PaymentGatewayTransactionEntity> findByTransactionRefAndDeletedFalse(String transactionRef);
}
