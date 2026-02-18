package com.shield.module.meeting.repository;

import com.shield.module.meeting.entity.MeetingResolutionEntity;
import com.shield.module.meeting.entity.MeetingResolutionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingResolutionRepository extends JpaRepository<MeetingResolutionEntity, UUID> {

    Optional<MeetingResolutionEntity> findByIdAndDeletedFalse(UUID id);

    List<MeetingResolutionEntity> findAllByMeetingIdAndDeletedFalseOrderByCreatedAtAsc(UUID meetingId);

    List<MeetingResolutionEntity> findAllByStatusAndDeletedFalse(MeetingResolutionStatus status);
}
