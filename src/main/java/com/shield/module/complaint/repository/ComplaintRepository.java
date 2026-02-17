package com.shield.module.complaint.repository;

import com.shield.module.complaint.entity.ComplaintEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<ComplaintEntity, UUID> {

    Page<ComplaintEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<ComplaintEntity> findByIdAndDeletedFalse(UUID id);
}
