package com.shield.module.meeting.repository;

import com.shield.module.meeting.entity.MeetingEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<MeetingEntity, UUID> {

    Page<MeetingEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<MeetingEntity> findByIdAndDeletedFalse(UUID id);

    Page<MeetingEntity> findAllByMeetingTypeIgnoreCaseAndDeletedFalse(String meetingType, Pageable pageable);

    Page<MeetingEntity> findAllByScheduledAtGreaterThanEqualAndDeletedFalse(Instant now, Pageable pageable);

    Page<MeetingEntity> findAllByScheduledAtLessThanAndDeletedFalse(Instant now, Pageable pageable);
}
