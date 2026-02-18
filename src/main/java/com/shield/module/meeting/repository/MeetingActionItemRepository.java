package com.shield.module.meeting.repository;

import com.shield.module.meeting.entity.MeetingActionItemEntity;
import com.shield.module.meeting.entity.MeetingActionItemStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingActionItemRepository extends JpaRepository<MeetingActionItemEntity, UUID> {

    Optional<MeetingActionItemEntity> findByIdAndDeletedFalse(UUID id);

    List<MeetingActionItemEntity> findAllByMeetingIdAndDeletedFalseOrderByCreatedAtAsc(UUID meetingId);

    Page<MeetingActionItemEntity> findAllByAssignedToAndDeletedFalse(UUID assignedTo, Pageable pageable);

    Page<MeetingActionItemEntity> findAllByStatusAndDeletedFalse(MeetingActionItemStatus status, Pageable pageable);
}
