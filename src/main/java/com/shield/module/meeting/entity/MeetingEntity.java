package com.shield.module.meeting.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "meeting")
public class MeetingEntity extends TenantAwareEntity {

    @Column(name = "meeting_number", nullable = false, length = 100)
    private String meetingNumber;

    @Column(name = "meeting_type", length = 100)
    private String meetingType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 2000)
    private String agenda;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(length = 255)
    private String location;

    @Column(name = "meeting_mode", length = 50)
    private String meetingMode;

    @Column(name = "meeting_link", length = 1000)
    private String meetingLink;

    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdBy;

    @Column(length = 8000)
    private String minutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MeetingStatus status;
}
