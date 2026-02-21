package com.shield.module.helpdesk.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "helpdesk_ticket_attachment")
public class HelpdeskTicketAttachmentEntity extends TenantAwareEntity {

    @Column(name = "ticket_id", nullable = false, columnDefinition = "uuid")
    private UUID ticketId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 2000)
    private String fileUrl;

    @Column(name = "uploaded_by", columnDefinition = "uuid")
    private UUID uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;
}
