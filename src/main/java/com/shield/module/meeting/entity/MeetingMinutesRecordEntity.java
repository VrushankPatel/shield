package com.shield.module.meeting.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "meeting_minutes_record")
public class MeetingMinutesRecordEntity extends TenantAwareEntity {

    @Column(name = "meeting_id", nullable = false, columnDefinition = "uuid")
    private UUID meetingId;

    @Column(name = "minutes_content", nullable = false, columnDefinition = "text")
    private String minutesContent;

    @Column(length = 4000)
    private String summary;

    @Column(name = "ai_generated_summary", length = 4000)
    private String aiGeneratedSummary;

    @Column(name = "prepared_by", columnDefinition = "uuid")
    private UUID preparedBy;

    @Column(name = "approved_by", columnDefinition = "uuid")
    private UUID approvedBy;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "document_url", length = 1000)
    private String documentUrl;
}
