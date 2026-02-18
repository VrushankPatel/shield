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
@Table(name = "meeting_attendee")
public class MeetingAttendeeEntity extends TenantAwareEntity {

    @Column(name = "meeting_id", nullable = false, columnDefinition = "uuid")
    private UUID meetingId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "invitation_sent_at")
    private Instant invitationSentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "rsvp_status", nullable = false, length = 50)
    private MeetingAttendeeRsvpStatus rsvpStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", length = 50)
    private MeetingAttendeeAttendanceStatus attendanceStatus;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;
}
