package com.shield.module.helpdesk.repository;

import com.shield.module.helpdesk.entity.HelpdeskCommentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HelpdeskCommentRepository extends JpaRepository<HelpdeskCommentEntity, UUID> {

    Page<HelpdeskCommentEntity> findAllByTicketIdAndDeletedFalse(UUID ticketId, Pageable pageable);

    Optional<HelpdeskCommentEntity> findByIdAndDeletedFalse(UUID id);
}
