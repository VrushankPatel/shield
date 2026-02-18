package com.shield.module.meeting.repository;

import com.shield.module.meeting.entity.MeetingReminderEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingReminderRepository extends JpaRepository<MeetingReminderEntity, UUID> {

    Optional<MeetingReminderEntity> findByIdAndDeletedFalse(UUID id);

    List<MeetingReminderEntity> findAllByMeetingIdAndDeletedFalseOrderBySentAtDesc(UUID meetingId);
}
