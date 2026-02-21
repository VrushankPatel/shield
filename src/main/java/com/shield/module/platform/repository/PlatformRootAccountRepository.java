package com.shield.module.platform.repository;

import com.shield.module.platform.entity.PlatformRootAccountEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformRootAccountRepository extends JpaRepository<PlatformRootAccountEntity, UUID> {

    Optional<PlatformRootAccountEntity> findByLoginIdAndDeletedFalse(String loginId);

    Optional<PlatformRootAccountEntity> findByIdAndDeletedFalse(UUID id);
}
