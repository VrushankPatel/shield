package com.shield.module.notification.repository;

import com.shield.module.notification.entity.NotificationEmailLogEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationEmailLogRepository extends JpaRepository<NotificationEmailLogEntity, UUID> {

    Page<NotificationEmailLogEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<NotificationEmailLogEntity> findByIdAndDeletedFalse(UUID id);
}
