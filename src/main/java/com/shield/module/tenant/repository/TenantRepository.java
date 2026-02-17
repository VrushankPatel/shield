package com.shield.module.tenant.repository;

import com.shield.module.tenant.entity.TenantEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<TenantEntity, UUID> {
}
