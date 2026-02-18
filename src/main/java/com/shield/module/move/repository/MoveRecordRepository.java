package com.shield.module.move.repository;

import com.shield.module.move.entity.MoveRecordEntity;
import com.shield.module.move.entity.MoveStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoveRecordRepository extends JpaRepository<MoveRecordEntity, UUID> {

    Optional<MoveRecordEntity> findByIdAndDeletedFalse(UUID id);

    Page<MoveRecordEntity> findAllByDeletedFalse(Pageable pageable);

    Page<MoveRecordEntity> findAllByUnitIdAndDeletedFalse(UUID unitId, Pageable pageable);

    Page<MoveRecordEntity> findAllByStatusAndDeletedFalse(MoveStatus status, Pageable pageable);
}
