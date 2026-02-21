package com.shield.module.platform.repository;

import com.shield.module.platform.entity.PlatformRootSessionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformRootSessionRepository extends JpaRepository<PlatformRootSessionEntity, UUID> {

    Optional<PlatformRootSessionEntity> findByTokenHashAndConsumedAtIsNullAndDeletedFalse(String tokenHash);

    List<PlatformRootSessionEntity> findAllByRootAccountIdAndConsumedAtIsNullAndDeletedFalse(UUID rootAccountId);
}
