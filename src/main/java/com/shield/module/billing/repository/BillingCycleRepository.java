package com.shield.module.billing.repository;

import com.shield.module.billing.entity.BillingCycleEntity;
import com.shield.module.billing.entity.BillingCycleStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingCycleRepository extends JpaRepository<BillingCycleEntity, UUID> {

    Optional<BillingCycleEntity> findByIdAndDeletedFalse(UUID id);

    Page<BillingCycleEntity> findAllByDeletedFalse(Pageable pageable);

    Page<BillingCycleEntity> findAllByYearAndDeletedFalse(Integer year, Pageable pageable);

    Optional<BillingCycleEntity> findFirstByStatusAndDeletedFalseOrderByYearDescMonthDesc(BillingCycleStatus status);
}
