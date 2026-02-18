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
@Table(name = "meeting_vote")
public class MeetingVoteEntity extends TenantAwareEntity {

    @Column(name = "resolution_id", nullable = false, columnDefinition = "uuid")
    private UUID resolutionId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MeetingVoteChoice vote;

    @Column(name = "voted_at", nullable = false)
    private Instant votedAt;
}
