package com.shield.module.accounting.repository;

import com.shield.module.accounting.entity.FundCategoryEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundCategoryRepository extends JpaRepository<FundCategoryEntity, UUID> {

    Optional<FundCategoryEntity> findByIdAndDeletedFalse(UUID id);

    Page<FundCategoryEntity> findAllByDeletedFalse(Pageable pageable);

    List<FundCategoryEntity> findAllByDeletedFalseOrderByCategoryNameAsc();
}
