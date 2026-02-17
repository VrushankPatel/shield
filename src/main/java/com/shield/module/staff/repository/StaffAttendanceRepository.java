package com.shield.module.staff.repository;

import com.shield.module.staff.entity.StaffAttendanceEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffAttendanceRepository extends JpaRepository<StaffAttendanceEntity, UUID> {

    Optional<StaffAttendanceEntity> findByStaffIdAndAttendanceDateAndDeletedFalse(UUID staffId, LocalDate attendanceDate);

    Page<StaffAttendanceEntity> findAllByStaffIdAndDeletedFalse(UUID staffId, Pageable pageable);

    List<StaffAttendanceEntity> findAllByStaffIdAndAttendanceDateBetweenAndDeletedFalse(
            UUID staffId,
            LocalDate fromDate,
            LocalDate toDate);
}
