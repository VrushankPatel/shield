package com.shield.module.payroll.repository;

import com.shield.module.payroll.entity.PayrollComponentEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollComponentRepository extends JpaRepository<PayrollComponentEntity, UUID> {

    Page<PayrollComponentEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<PayrollComponentEntity> findByIdAndDeletedFalse(UUID id);

    boolean existsByComponentNameIgnoreCaseAndDeletedFalse(String componentName);

    boolean existsByComponentNameIgnoreCaseAndDeletedFalseAndIdNot(String componentName, UUID id);

    List<PayrollComponentEntity> findAllByIdInAndDeletedFalse(Collection<UUID> ids);
}
