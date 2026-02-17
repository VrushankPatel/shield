package com.shield.module.accounting.repository;

import com.shield.module.accounting.entity.LedgerEntryEntity;
import com.shield.module.accounting.entity.LedgerType;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, UUID> {

    Page<LedgerEntryEntity> findAllByDeletedFalse(Pageable pageable);

    @Query("""
            select e.type, coalesce(sum(e.amount), 0)
            from LedgerEntryEntity e
            where e.deleted = false
            group by e.type
            """)
    List<Object[]> summarizeByType();
}
