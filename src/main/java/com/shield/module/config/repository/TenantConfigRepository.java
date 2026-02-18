package com.shield.module.config.repository;

import com.shield.module.config.entity.TenantConfigEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantConfigRepository extends JpaRepository<TenantConfigEntity, UUID> {

    Page<TenantConfigEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<TenantConfigEntity> findFirstByConfigKeyAndDeletedFalseOrderByCreatedAtDesc(String configKey);

    Page<TenantConfigEntity> findAllByCategoryAndDeletedFalse(String category, Pageable pageable);
}
