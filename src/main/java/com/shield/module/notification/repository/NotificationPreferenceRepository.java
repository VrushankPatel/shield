package com.shield.module.notification.repository;

import com.shield.module.notification.entity.NotificationPreferenceEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreferenceEntity, UUID> {

    Optional<NotificationPreferenceEntity> findByTenantIdAndUserIdAndDeletedFalse(UUID tenantId, UUID userId);
}
