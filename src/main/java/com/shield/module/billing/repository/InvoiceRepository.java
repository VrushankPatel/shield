package com.shield.module.billing.repository;

import com.shield.module.billing.entity.InvoiceEntity;
import com.shield.module.billing.entity.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {

    Optional<InvoiceEntity> findByIdAndDeletedFalse(UUID id);

    Optional<InvoiceEntity> findByInvoiceNumberAndDeletedFalse(String invoiceNumber);

    Page<InvoiceEntity> findAllByDeletedFalse(Pageable pageable);

    List<InvoiceEntity> findAllByUnitIdAndDeletedFalse(UUID unitId);

    List<InvoiceEntity> findAllByBillingCycleIdAndDeletedFalse(UUID billingCycleId);

    Page<InvoiceEntity> findAllByStatusAndDeletedFalse(InvoiceStatus status, Pageable pageable);

    Page<InvoiceEntity> findAllByOutstandingAmountGreaterThanAndDeletedFalse(BigDecimal outstandingAmount, Pageable pageable);

    Page<InvoiceEntity> findAllByStatusInAndDeletedFalse(List<InvoiceStatus> statuses, Pageable pageable);

    Page<InvoiceEntity> findAllByDueDateBeforeAndOutstandingAmountGreaterThanAndDeletedFalse(
            LocalDate dueDate,
            BigDecimal outstandingAmount,
            Pageable pageable);
}
