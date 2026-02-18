package com.shield.module.complaint.entity;

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
@Table(name = "complaint_comment")
public class ComplaintCommentEntity extends TenantAwareEntity {

    @Column(name = "complaint_id", nullable = false, columnDefinition = "uuid")
    private UUID complaintId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, length = 2000)
    private String comment;
}
