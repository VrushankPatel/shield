package com.shield.module.meeting.repository;

import com.shield.module.meeting.entity.MeetingAttendeeEntity;
import com.shield.module.meeting.entity.MeetingAttendeeRsvpStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingAttendeeRepository extends JpaRepository<MeetingAttendeeEntity, UUID> {

    List<MeetingAttendeeEntity> findAllByMeetingIdAndDeletedFalseOrderByCreatedAtAsc(UUID meetingId);

    Optional<MeetingAttendeeEntity> findByIdAndDeletedFalse(UUID id);

    Optional<MeetingAttendeeEntity> findFirstByMeetingIdAndUserIdAndDeletedFalse(UUID meetingId, UUID userId);

    long countByMeetingIdAndRsvpStatusAndDeletedFalse(UUID meetingId, MeetingAttendeeRsvpStatus rsvpStatus);

    Page<MeetingAttendeeEntity> findAllByUserIdAndDeletedFalse(UUID userId, Pageable pageable);
}
