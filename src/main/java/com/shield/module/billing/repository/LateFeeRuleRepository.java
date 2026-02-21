package com.shield.module.billing.repository;

import com.shield.module.billing.entity.LateFeeRuleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LateFeeRuleRepository extends JpaRepository<LateFeeRuleEntity, UUID> {

    Optional<LateFeeRuleEntity> findByIdAndDeletedFalse(UUID id);

    Page<LateFeeRuleEntity> findAllByDeletedFalse(Pageable pageable);
}
