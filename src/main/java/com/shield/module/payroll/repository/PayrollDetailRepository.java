package com.shield.module.payroll.repository;

import com.shield.module.payroll.entity.PayrollDetailEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayrollDetailRepository extends JpaRepository<PayrollDetailEntity, UUID> {

    List<PayrollDetailEntity> findAllByPayrollIdAndDeletedFalse(UUID payrollId);

    List<PayrollDetailEntity> findAllByPayrollIdAndDeletedFalseOrderByCreatedAtAsc(UUID payrollId);
}
