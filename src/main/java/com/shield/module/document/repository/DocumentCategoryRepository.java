package com.shield.module.document.repository;

import com.shield.module.document.entity.DocumentCategoryEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentCategoryRepository extends JpaRepository<DocumentCategoryEntity, UUID> {

    Page<DocumentCategoryEntity> findAllByDeletedFalse(Pageable pageable);

    List<DocumentCategoryEntity> findAllByDeletedFalse();

    Optional<DocumentCategoryEntity> findByIdAndDeletedFalse(UUID id);
}
