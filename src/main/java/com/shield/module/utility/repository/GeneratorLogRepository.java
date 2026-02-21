package com.shield.module.utility.repository;

import com.shield.module.utility.entity.GeneratorLogEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratorLogRepository extends JpaRepository<GeneratorLogEntity, UUID> {

    Page<GeneratorLogEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<GeneratorLogEntity> findByIdAndDeletedFalse(UUID id);

    Page<GeneratorLogEntity> findAllByGeneratorIdAndDeletedFalse(UUID generatorId, Pageable pageable);

    Page<GeneratorLogEntity> findAllByLogDateBetweenAndDeletedFalse(LocalDate from, LocalDate to, Pageable pageable);

    Page<GeneratorLogEntity> findAllByGeneratorIdAndLogDateBetweenAndDeletedFalse(
            UUID generatorId,
            LocalDate from,
            LocalDate to,
            Pageable pageable);

    List<GeneratorLogEntity> findAllByLogDateBetweenAndDeletedFalse(LocalDate from, LocalDate to);

    List<GeneratorLogEntity> findAllByGeneratorIdAndLogDateBetweenAndDeletedFalse(UUID generatorId, LocalDate from, LocalDate to);
}
