package com.shield.module.visitor.repository;

import com.shield.module.visitor.entity.DomesticHelpEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomesticHelpRepository extends JpaRepository<DomesticHelpEntity, UUID> {

    Optional<DomesticHelpEntity> findByIdAndDeletedFalse(UUID id);

    Page<DomesticHelpEntity> findAllByDeletedFalse(Pageable pageable);

    Page<DomesticHelpEntity> findAllByHelpTypeAndDeletedFalse(String helpType, Pageable pageable);
}
