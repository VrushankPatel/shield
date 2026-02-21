package com.shield.module.document.repository;

import com.shield.module.document.entity.DocumentEntity;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

    Page<DocumentEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<DocumentEntity> findByIdAndDeletedFalse(UUID id);

    Page<DocumentEntity> findAllByPublicAccessTrueAndDeletedFalse(Pageable pageable);

    Page<DocumentEntity> findAllByCategoryIdAndDeletedFalse(UUID categoryId, Pageable pageable);

    Page<DocumentEntity> findAllByExpiryDateBetweenAndDeletedFalse(LocalDate from, LocalDate to, Pageable pageable);

    @Query("""
            select d from DocumentEntity d
            where d.deleted = false
              and (
                  lower(d.documentName) like lower(concat('%', :query, '%'))
                  or lower(coalesce(d.description, '')) like lower(concat('%', :query, '%'))
                  or lower(coalesce(d.tags, '')) like lower(concat('%', :query, '%'))
              )
            """)
    Page<DocumentEntity> searchByQuery(@Param("query") String query, Pageable pageable);

    @Query("""
            select d from DocumentEntity d
            where d.deleted = false
              and lower(concat(',', coalesce(d.tags, ''), ',')) like lower(concat('%,', :tag, ',%'))
            """)
    Page<DocumentEntity> findAllByTag(@Param("tag") String tag, Pageable pageable);
}
