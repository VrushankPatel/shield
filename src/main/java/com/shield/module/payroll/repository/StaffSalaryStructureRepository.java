package com.shield.module.payroll.repository;

import com.shield.module.payroll.entity.StaffSalaryStructureEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffSalaryStructureRepository extends JpaRepository<StaffSalaryStructureEntity, UUID> {

    Page<StaffSalaryStructureEntity> findAllByStaffIdAndDeletedFalse(UUID staffId, Pageable pageable);

    List<StaffSalaryStructureEntity> findAllByStaffIdAndDeletedFalse(UUID staffId);

    List<StaffSalaryStructureEntity> findAllByStaffIdAndActiveTrueAndEffectiveFromLessThanEqualAndDeletedFalse(
            UUID staffId,
            LocalDate effectiveFrom);

    Optional<StaffSalaryStructureEntity> findByIdAndDeletedFalse(UUID id);

    boolean existsByStaffIdAndPayrollComponentIdAndEffectiveFromAndDeletedFalse(
            UUID staffId,
            UUID payrollComponentId,
            LocalDate effectiveFrom);

    boolean existsByStaffIdAndPayrollComponentIdAndEffectiveFromAndDeletedFalseAndIdNot(
            UUID staffId,
            UUID payrollComponentId,
            LocalDate effectiveFrom,
            UUID id);
}
