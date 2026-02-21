package com.shield.module.billing.repository;

import com.shield.module.billing.entity.PaymentEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByIdAndDeletedFalse(UUID id);

    Optional<PaymentEntity> findByTransactionRefAndDeletedFalse(String transactionRef);

    Page<PaymentEntity> findAllByDeletedFalse(Pageable pageable);

    List<PaymentEntity> findAllByInvoiceIdAndDeletedFalse(UUID invoiceId);

    List<PaymentEntity> findAllByUnitIdAndDeletedFalse(UUID unitId);
}
