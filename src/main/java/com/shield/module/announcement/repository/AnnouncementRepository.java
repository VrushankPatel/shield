package com.shield.module.announcement.repository;

import com.shield.module.announcement.entity.AnnouncementEntity;
import com.shield.module.announcement.entity.AnnouncementCategory;
import com.shield.module.announcement.entity.AnnouncementPriority;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnnouncementRepository extends JpaRepository<AnnouncementEntity, UUID> {

    Page<AnnouncementEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<AnnouncementEntity> findByIdAndDeletedFalse(UUID id);

    Page<AnnouncementEntity> findAllByCategoryAndDeletedFalse(AnnouncementCategory category, Pageable pageable);

    Page<AnnouncementEntity> findAllByPriorityAndDeletedFalse(AnnouncementPriority priority, Pageable pageable);

    @Query("""
            SELECT a
            FROM AnnouncementEntity a
            WHERE a.deleted = false
              AND a.status = com.shield.module.announcement.entity.AnnouncementStatus.PUBLISHED
              AND (a.expiresAt IS NULL OR a.expiresAt > :now)
            """)
    Page<AnnouncementEntity> findAllActive(@Param("now") Instant now, Pageable pageable);
}
