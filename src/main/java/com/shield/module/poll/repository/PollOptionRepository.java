package com.shield.module.poll.repository;

import com.shield.module.poll.entity.PollOptionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollOptionRepository extends JpaRepository<PollOptionEntity, UUID> {

    List<PollOptionEntity> findAllByPollIdAndDeletedFalse(UUID pollId);

    Optional<PollOptionEntity> findByIdAndDeletedFalse(UUID id);
}
