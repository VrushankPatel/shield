package com.shield.module.payroll.repository;

import com.shield.module.payroll.entity.PayrollEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollRepository extends JpaRepository<PayrollEntity, UUID> {

    Page<PayrollEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<PayrollEntity> findByIdAndDeletedFalse(UUID id);

    Optional<PayrollEntity> findByStaffIdAndYearAndMonthAndDeletedFalse(UUID staffId, int year, int month);
}
