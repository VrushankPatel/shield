package com.shield.module.helpdesk.repository;

import com.shield.module.helpdesk.entity.HelpdeskTicketEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HelpdeskTicketRepository extends JpaRepository<HelpdeskTicketEntity, UUID> {

    Page<HelpdeskTicketEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<HelpdeskTicketEntity> findByIdAndDeletedFalse(UUID id);
}
