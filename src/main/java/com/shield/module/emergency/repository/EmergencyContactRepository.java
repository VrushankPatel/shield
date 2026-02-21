package com.shield.module.emergency.repository;

import com.shield.module.emergency.entity.EmergencyContactEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContactEntity, UUID> {

    Page<EmergencyContactEntity> findAllByDeletedFalse(Pageable pageable);

    Page<EmergencyContactEntity> findAllByContactTypeIgnoreCaseAndDeletedFalse(String contactType, Pageable pageable);

    Optional<EmergencyContactEntity> findByIdAndDeletedFalse(UUID id);
}
