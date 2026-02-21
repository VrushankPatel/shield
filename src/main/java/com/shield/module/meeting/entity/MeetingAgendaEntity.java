package com.shield.module.meeting.entity;

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
@Table(name = "meeting_agenda")
public class MeetingAgendaEntity extends TenantAwareEntity {

    @Column(name = "meeting_id", nullable = false, columnDefinition = "uuid")
    private UUID meetingId;

    @Column(name = "agenda_item", nullable = false, length = 255)
    private String agendaItem;

    @Column(length = 2000)
    private String description;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(columnDefinition = "uuid")
    private UUID presenter;

    @Column(name = "estimated_duration")
    private Integer estimatedDuration;
}
