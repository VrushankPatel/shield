package com.shield.module.announcement.repository;

import com.shield.module.announcement.entity.AnnouncementAttachmentEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementAttachmentRepository extends JpaRepository<AnnouncementAttachmentEntity, UUID> {

    List<AnnouncementAttachmentEntity> findAllByAnnouncementIdAndDeletedFalse(UUID announcementId);

    Optional<AnnouncementAttachmentEntity> findByIdAndDeletedFalse(UUID id);
}
