package com.shield.module.helpdesk.repository;

import com.shield.module.helpdesk.entity.HelpdeskTicketEntity;
import com.shield.module.helpdesk.entity.TicketStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HelpdeskTicketRepository extends JpaRepository<HelpdeskTicketEntity, UUID> {

    Page<HelpdeskTicketEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<HelpdeskTicketEntity> findByIdAndDeletedFalse(UUID id);

    Page<HelpdeskTicketEntity> findAllByRaisedByAndDeletedFalse(UUID raisedBy, Pageable pageable);

    Page<HelpdeskTicketEntity> findAllByAssignedToAndDeletedFalse(UUID assignedTo, Pageable pageable);

    Page<HelpdeskTicketEntity> findAllByStatusAndDeletedFalse(TicketStatus status, Pageable pageable);

    List<HelpdeskTicketEntity> findAllByDeletedFalse();
}
