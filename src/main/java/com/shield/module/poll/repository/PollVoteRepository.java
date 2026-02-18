package com.shield.module.poll.repository;

import com.shield.module.poll.entity.PollVoteEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollVoteRepository extends JpaRepository<PollVoteEntity, UUID> {

    boolean existsByPollIdAndUserIdAndDeletedFalse(UUID pollId, UUID userId);

    Optional<PollVoteEntity> findByPollIdAndUserIdAndDeletedFalse(UUID pollId, UUID userId);

    List<PollVoteEntity> findAllByPollIdAndDeletedFalse(UUID pollId);

    long countByOptionIdAndDeletedFalse(UUID optionId);
}
