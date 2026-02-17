package com.shield.module.meeting.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "meeting")
public class MeetingEntity extends TenantAwareEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 2000)
    private String agenda;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(length = 8000)
    private String minutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MeetingStatus status;
}
