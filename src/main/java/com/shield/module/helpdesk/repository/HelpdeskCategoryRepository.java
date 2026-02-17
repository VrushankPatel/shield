package com.shield.module.helpdesk.repository;

import com.shield.module.helpdesk.entity.HelpdeskCategoryEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HelpdeskCategoryRepository extends JpaRepository<HelpdeskCategoryEntity, UUID> {

    Page<HelpdeskCategoryEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<HelpdeskCategoryEntity> findByIdAndDeletedFalse(UUID id);
}
