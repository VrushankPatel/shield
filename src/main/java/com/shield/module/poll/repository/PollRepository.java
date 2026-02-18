package com.shield.module.poll.repository;

import com.shield.module.poll.entity.PollEntity;
import com.shield.module.poll.entity.PollStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollRepository extends JpaRepository<PollEntity, UUID> {

    Page<PollEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<PollEntity> findByIdAndDeletedFalse(UUID id);

    Page<PollEntity> findAllByStatusAndDeletedFalse(PollStatus status, Pageable pageable);
}
