package com.shield.module.helpdesk.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "helpdesk_comment")
public class HelpdeskCommentEntity extends TenantAwareEntity {

    @Column(name = "ticket_id", nullable = false, columnDefinition = "uuid")
    private UUID ticketId;

    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, length = 2000)
    private String comment;

    @Column(name = "internal_note", nullable = false)
    private boolean internalNote;
}
