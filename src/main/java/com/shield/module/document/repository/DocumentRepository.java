package com.shield.module.document.repository;

import com.shield.module.document.entity.DocumentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

    Page<DocumentEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<DocumentEntity> findByIdAndDeletedFalse(UUID id);

    Page<DocumentEntity> findAllByPublicAccessTrueAndDeletedFalse(Pageable pageable);

    Page<DocumentEntity> findAllByCategoryIdAndDeletedFalse(UUID categoryId, Pageable pageable);
}
