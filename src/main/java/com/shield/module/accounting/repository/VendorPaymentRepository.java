package com.shield.module.accounting.repository;

import com.shield.module.accounting.entity.VendorPaymentEntity;
import com.shield.module.accounting.entity.VendorPaymentStatus;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VendorPaymentRepository extends JpaRepository<VendorPaymentEntity, UUID> {

    Optional<VendorPaymentEntity> findByIdAndDeletedFalse(UUID id);

    Page<VendorPaymentEntity> findAllByDeletedFalse(Pageable pageable);

    Page<VendorPaymentEntity> findAllByVendorIdAndDeletedFalse(UUID vendorId, Pageable pageable);

    Page<VendorPaymentEntity> findAllByExpenseIdAndDeletedFalse(UUID expenseId, Pageable pageable);

    Page<VendorPaymentEntity> findAllByStatusAndDeletedFalse(VendorPaymentStatus status, Pageable pageable);

    @Query("""
            select coalesce(sum(v.amount), 0)
            from VendorPaymentEntity v
            where v.deleted = false
              and v.status = :status
            """)
    BigDecimal sumAmountByStatus(@Param("status") VendorPaymentStatus status);
}
