package com.shield.module.meeting.repository;

import com.shield.module.meeting.entity.MeetingMinutesRecordEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingMinutesRecordRepository extends JpaRepository<MeetingMinutesRecordEntity, UUID> {

    Optional<MeetingMinutesRecordEntity> findByIdAndDeletedFalse(UUID id);

    Optional<MeetingMinutesRecordEntity> findFirstByMeetingIdAndDeletedFalseOrderByCreatedAtDesc(UUID meetingId);
}
