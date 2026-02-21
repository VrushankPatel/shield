package com.shield.module.accounting.repository;

import com.shield.module.accounting.entity.LedgerEntryEntity;
import com.shield.module.accounting.entity.LedgerTransactionType;
import com.shield.module.accounting.entity.LedgerType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, UUID> {

    Optional<LedgerEntryEntity> findByIdAndDeletedFalse(UUID id);

    Page<LedgerEntryEntity> findAllByDeletedFalse(Pageable pageable);

    Page<LedgerEntryEntity> findAllByTenantIdAndDeletedFalse(UUID tenantId, Pageable pageable);

    List<LedgerEntryEntity> findAllByDeletedFalseOrderByEntryDateDesc();

    Page<LedgerEntryEntity> findAllByAccountHeadIdAndDeletedFalse(UUID accountHeadId, Pageable pageable);

    Page<LedgerEntryEntity> findAllByFundCategoryIdAndDeletedFalse(UUID fundCategoryId, Pageable pageable);

    Page<LedgerEntryEntity> findAllByEntryDateBetweenAndDeletedFalse(LocalDate fromDate, LocalDate toDate, Pageable pageable);

    @Query("""
            select e.type, coalesce(sum(e.amount), 0)
            from LedgerEntryEntity e
            where e.deleted = false
            group by e.type
            """)
    List<Object[]> summarizeByType();

    @Query("""
            select e.type, coalesce(sum(e.amount), 0)
            from LedgerEntryEntity e
            where e.deleted = false
              and e.tenantId = :tenantId
            group by e.type
            """)
    List<Object[]> summarizeByTenantId(UUID tenantId);

    @Query("""
            select coalesce(sum(e.amount), 0)
            from LedgerEntryEntity e
            where e.deleted = false
              and e.type = :type
            """)
    BigDecimal sumAmountByType(LedgerType type);

    @Query("""
            select coalesce(sum(e.amount), 0)
            from LedgerEntryEntity e
            where e.deleted = false
              and e.transactionType = :transactionType
            """)
    BigDecimal sumAmountByTransactionType(LedgerTransactionType transactionType);

    @Query("""
            select coalesce(sum(e.amount), 0)
            from LedgerEntryEntity e
            where e.deleted = false
              and e.accountHeadId = :accountHeadId
            """)
    BigDecimal sumAmountByAccountHeadId(UUID accountHeadId);
}
