package com.shield.module.marketplace.repository;

import com.shield.module.marketplace.entity.MarketplaceCategoryEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketplaceCategoryRepository extends JpaRepository<MarketplaceCategoryEntity, UUID> {

    Page<MarketplaceCategoryEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<MarketplaceCategoryEntity> findByIdAndDeletedFalse(UUID id);
}
