package com.shield.module.announcement.repository;

import com.shield.module.announcement.entity.AnnouncementReadReceiptEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementReadReceiptRepository extends JpaRepository<AnnouncementReadReceiptEntity, UUID> {

    Optional<AnnouncementReadReceiptEntity> findByAnnouncementIdAndUserIdAndDeletedFalse(UUID announcementId, UUID userId);

    Page<AnnouncementReadReceiptEntity> findAllByAnnouncementIdAndDeletedFalse(UUID announcementId, Pageable pageable);

    long countByAnnouncementIdAndDeletedFalse(UUID announcementId);
}
