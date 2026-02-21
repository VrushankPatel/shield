package com.shield.module.staff.repository;

import com.shield.module.staff.entity.StaffLeaveEntity;
import com.shield.module.staff.entity.StaffLeaveStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffLeaveRepository extends JpaRepository<StaffLeaveEntity, UUID> {

    Page<StaffLeaveEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<StaffLeaveEntity> findByIdAndDeletedFalse(UUID id);

    Page<StaffLeaveEntity> findAllByStaffIdAndDeletedFalse(UUID staffId, Pageable pageable);

    List<StaffLeaveEntity> findAllByStaffIdAndDeletedFalse(UUID staffId);

    Page<StaffLeaveEntity> findAllByStatusAndDeletedFalse(StaffLeaveStatus status, Pageable pageable);
}
