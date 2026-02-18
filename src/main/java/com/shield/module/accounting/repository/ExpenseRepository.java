package com.shield.module.accounting.repository;

import com.shield.module.accounting.entity.ExpenseEntity;
import com.shield.module.accounting.entity.ExpensePaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, UUID> {

    Optional<ExpenseEntity> findByIdAndDeletedFalse(UUID id);

    Optional<ExpenseEntity> findByExpenseNumberAndDeletedFalse(String expenseNumber);

    Page<ExpenseEntity> findAllByDeletedFalse(Pageable pageable);

    Page<ExpenseEntity> findAllByPaymentStatusAndDeletedFalse(ExpensePaymentStatus paymentStatus, Pageable pageable);

    Page<ExpenseEntity> findAllByVendorIdAndDeletedFalse(UUID vendorId, Pageable pageable);

    Page<ExpenseEntity> findAllByAccountHeadIdAndDeletedFalse(UUID accountHeadId, Pageable pageable);

    Page<ExpenseEntity> findAllByExpenseDateBetweenAndDeletedFalse(LocalDate from, LocalDate to, Pageable pageable);

    @Query("""
            select coalesce(sum(e.amount), 0)
            from ExpenseEntity e
            where e.deleted = false
              and e.accountHeadId = :accountHeadId
              and e.paymentStatus = :paymentStatus
              and e.expenseDate between :fromDate and :toDate
            """)
    BigDecimal sumAmountByAccountHeadAndStatusAndDateRange(
            @Param("accountHeadId") UUID accountHeadId,
            @Param("paymentStatus") ExpensePaymentStatus paymentStatus,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query("""
            select coalesce(sum(e.amount), 0)
            from ExpenseEntity e
            where e.deleted = false
              and e.paymentStatus = :paymentStatus
              and e.expenseDate between :fromDate and :toDate
            """)
    BigDecimal sumAmountByStatusAndDateRange(
            @Param("paymentStatus") ExpensePaymentStatus paymentStatus,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
