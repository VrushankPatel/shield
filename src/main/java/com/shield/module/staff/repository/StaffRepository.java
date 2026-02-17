package com.shield.module.staff.repository;

import com.shield.module.staff.entity.StaffEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<StaffEntity, UUID> {

    Page<StaffEntity> findAllByDeletedFalse(Pageable pageable);

    Page<StaffEntity> findAllByActiveTrueAndDeletedFalse(Pageable pageable);

    Optional<StaffEntity> findByIdAndDeletedFalse(UUID id);
}
