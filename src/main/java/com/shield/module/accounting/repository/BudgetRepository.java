package com.shield.module.accounting.repository;

import com.shield.module.accounting.entity.BudgetEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<BudgetEntity, UUID> {

    Optional<BudgetEntity> findByIdAndDeletedFalse(UUID id);

    Page<BudgetEntity> findAllByDeletedFalse(Pageable pageable);

    List<BudgetEntity> findAllByFinancialYearAndDeletedFalse(String financialYear);
}
