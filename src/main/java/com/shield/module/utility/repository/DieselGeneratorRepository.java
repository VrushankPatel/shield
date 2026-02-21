package com.shield.module.utility.repository;

import com.shield.module.utility.entity.DieselGeneratorEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DieselGeneratorRepository extends JpaRepository<DieselGeneratorEntity, UUID> {

    Page<DieselGeneratorEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<DieselGeneratorEntity> findByIdAndDeletedFalse(UUID id);
}
