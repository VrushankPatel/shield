package com.shield.module.notification.repository;

import com.shield.module.notification.entity.NotificationEmailLogEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationEmailLogRepository extends JpaRepository<NotificationEmailLogEntity, UUID> {

    Page<NotificationEmailLogEntity> findAllByDeletedFalse(Pageable pageable);

    Page<NotificationEmailLogEntity> findAllByUserIdAndDeletedFalse(UUID userId, Pageable pageable);

    Optional<NotificationEmailLogEntity> findByIdAndDeletedFalse(UUID id);

    Optional<NotificationEmailLogEntity> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);

    long countByUserIdAndReadAtIsNullAndDeletedFalse(UUID userId);

    List<NotificationEmailLogEntity> findAllByUserIdAndDeletedFalse(UUID userId);
}
