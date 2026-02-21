package com.shield.module.meeting.repository;

import com.shield.module.meeting.entity.MeetingVoteEntity;
import com.shield.module.meeting.entity.MeetingVoteChoice;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingVoteRepository extends JpaRepository<MeetingVoteEntity, UUID> {

    Optional<MeetingVoteEntity> findByIdAndDeletedFalse(UUID id);

    Optional<MeetingVoteEntity> findFirstByResolutionIdAndUserIdAndDeletedFalse(UUID resolutionId, UUID userId);

    List<MeetingVoteEntity> findAllByResolutionIdAndDeletedFalseOrderByCreatedAtAsc(UUID resolutionId);

    long countByResolutionIdAndVoteAndDeletedFalse(UUID resolutionId, MeetingVoteChoice vote);
}
