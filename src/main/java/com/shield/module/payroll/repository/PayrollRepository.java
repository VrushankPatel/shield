package com.shield.module.payroll.repository;

import com.shield.module.payroll.entity.PayrollEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PayrollRepository extends JpaRepository<PayrollEntity, UUID> {

    Page<PayrollEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<PayrollEntity> findByIdAndDeletedFalse(UUID id);

    Optional<PayrollEntity> findByStaffIdAndYearAndMonthAndDeletedFalse(UUID staffId, int year, int month);

    Page<PayrollEntity> findAllByStaffIdAndDeletedFalse(UUID staffId, Pageable pageable);

    Page<PayrollEntity> findAllByYearAndMonthAndDeletedFalse(int year, int month, Pageable pageable);

    @Query("""
            SELECT COALESCE(COUNT(p), 0),
                   COALESCE(SUM(p.grossSalary), 0),
                   COALESCE(SUM(p.totalDeductions), 0),
                   COALESCE(SUM(p.netSalary), 0)
            FROM PayrollEntity p
            WHERE p.deleted = false
              AND (:year IS NULL OR p.year = :year)
              AND (:month IS NULL OR p.month = :month)
            """)
    List<Object[]> summarize(@Param("year") Integer year, @Param("month") Integer month);
}
