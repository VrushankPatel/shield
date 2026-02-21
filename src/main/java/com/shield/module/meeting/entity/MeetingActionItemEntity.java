package com.shield.module.meeting.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "meeting_action_item")
public class MeetingActionItemEntity extends TenantAwareEntity {

    @Column(name = "meeting_id", nullable = false, columnDefinition = "uuid")
    private UUID meetingId;

    @Column(name = "action_description", nullable = false, length = 2000)
    private String actionDescription;

    @Column(name = "assigned_to", columnDefinition = "uuid")
    private UUID assignedTo;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(length = 20)
    private String priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MeetingActionItemStatus status;

    @Column(name = "completion_date")
    private LocalDate completionDate;
}
