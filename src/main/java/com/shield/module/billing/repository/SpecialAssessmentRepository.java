package com.shield.module.billing.repository;

import com.shield.module.billing.entity.SpecialAssessmentEntity;
import com.shield.module.billing.entity.SpecialAssessmentStatus;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialAssessmentRepository extends JpaRepository<SpecialAssessmentEntity, UUID> {

    Optional<SpecialAssessmentEntity> findByIdAndDeletedFalse(UUID id);

    Page<SpecialAssessmentEntity> findAllByDeletedFalse(Pageable pageable);

    Page<SpecialAssessmentEntity> findAllByStatusAndDueDateGreaterThanEqualAndDeletedFalse(
            SpecialAssessmentStatus status,
            LocalDate dueDate,
            Pageable pageable);
}
