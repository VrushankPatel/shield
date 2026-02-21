package com.shield.module.complaint.repository;

import com.shield.module.complaint.entity.ComplaintCommentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintCommentRepository extends JpaRepository<ComplaintCommentEntity, UUID> {

    Page<ComplaintCommentEntity> findAllByComplaintIdAndDeletedFalse(UUID complaintId, Pageable pageable);

    Optional<ComplaintCommentEntity> findByIdAndDeletedFalse(UUID id);
}
