package com.shield.module.meeting.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "meeting_resolution")
public class MeetingResolutionEntity extends TenantAwareEntity {

    @Column(name = "meeting_id", nullable = false, columnDefinition = "uuid")
    private UUID meetingId;

    @Column(name = "resolution_number", nullable = false, length = 100)
    private String resolutionNumber;

    @Column(name = "resolution_text", nullable = false, columnDefinition = "text")
    private String resolutionText;

    @Column(name = "proposed_by", columnDefinition = "uuid")
    private UUID proposedBy;

    @Column(name = "seconded_by", columnDefinition = "uuid")
    private UUID secondedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MeetingResolutionStatus status;

    @Column(name = "votes_for", nullable = false)
    private Integer votesFor = 0;

    @Column(name = "votes_against", nullable = false)
    private Integer votesAgainst = 0;

    @Column(name = "votes_abstain", nullable = false)
    private Integer votesAbstain = 0;
}
