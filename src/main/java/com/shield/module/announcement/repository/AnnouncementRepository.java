package com.shield.module.announcement.repository;

import com.shield.module.announcement.entity.AnnouncementEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<AnnouncementEntity, UUID> {

    Page<AnnouncementEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<AnnouncementEntity> findByIdAndDeletedFalse(UUID id);
}
