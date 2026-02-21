package com.shield.module.helpdesk.repository;

import com.shield.module.helpdesk.entity.HelpdeskTicketAttachmentEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HelpdeskTicketAttachmentRepository extends JpaRepository<HelpdeskTicketAttachmentEntity, UUID> {

    Page<HelpdeskTicketAttachmentEntity> findAllByTicketIdAndDeletedFalse(UUID ticketId, Pageable pageable);

    Optional<HelpdeskTicketAttachmentEntity> findByIdAndDeletedFalse(UUID id);
}
