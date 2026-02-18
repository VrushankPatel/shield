package com.shield.module.complaint.repository;

import com.shield.module.complaint.entity.WorkOrderEntity;
import com.shield.module.complaint.entity.WorkOrderStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkOrderRepository extends JpaRepository<WorkOrderEntity, UUID> {

    Page<WorkOrderEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<WorkOrderEntity> findByIdAndDeletedFalse(UUID id);

    Page<WorkOrderEntity> findAllByComplaintIdAndDeletedFalse(UUID complaintId, Pageable pageable);

    Page<WorkOrderEntity> findAllByVendorIdAndDeletedFalse(UUID vendorId, Pageable pageable);

    Page<WorkOrderEntity> findAllByStatusAndDeletedFalse(WorkOrderStatus status, Pageable pageable);
}
