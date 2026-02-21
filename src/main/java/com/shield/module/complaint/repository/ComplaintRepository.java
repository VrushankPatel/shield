package com.shield.module.complaint.repository;

import com.shield.module.complaint.entity.ComplaintEntity;
import com.shield.module.complaint.entity.ComplaintPriority;
import com.shield.module.complaint.entity.ComplaintStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<ComplaintEntity, UUID> {

    Page<ComplaintEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<ComplaintEntity> findByIdAndDeletedFalse(UUID id);

    Page<ComplaintEntity> findAllByStatusAndDeletedFalse(ComplaintStatus status, Pageable pageable);

    Page<ComplaintEntity> findAllByPriorityAndDeletedFalse(ComplaintPriority priority, Pageable pageable);

    Page<ComplaintEntity> findAllByAssetIdAndDeletedFalse(UUID assetId, Pageable pageable);

    Page<ComplaintEntity> findAllByRaisedByAndDeletedFalse(UUID raisedBy, Pageable pageable);

    Page<ComplaintEntity> findAllByAssignedToAndDeletedFalse(UUID assignedTo, Pageable pageable);

    Page<ComplaintEntity> findAllBySlaBreachTrueAndDeletedFalse(Pageable pageable);

    List<ComplaintEntity> findAllByDeletedFalse();
}
