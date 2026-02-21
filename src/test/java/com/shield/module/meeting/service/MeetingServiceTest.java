package com.shield.module.meeting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.meeting.dto.MeetingAttendanceReportResponse;
import com.shield.module.meeting.dto.MeetingCreateRequest;
import com.shield.module.meeting.dto.MeetingMinutesUpdateRequest;
import com.shield.module.meeting.dto.MeetingResponse;
import com.shield.module.meeting.dto.MeetingVoteRequest;
import com.shield.module.meeting.dto.MeetingVoteResponse;
import com.shield.module.meeting.entity.MeetingAttendeeAttendanceStatus;
import com.shield.module.meeting.entity.MeetingAttendeeEntity;
import com.shield.module.meeting.entity.MeetingAttendeeRsvpStatus;
import com.shield.module.meeting.entity.MeetingEntity;
import com.shield.module.meeting.entity.MeetingResolutionEntity;
import com.shield.module.meeting.entity.MeetingResolutionStatus;
import com.shield.module.meeting.entity.MeetingStatus;
import com.shield.module.meeting.entity.MeetingVoteChoice;
import com.shield.module.meeting.entity.MeetingVoteEntity;
import com.shield.module.meeting.repository.MeetingActionItemRepository;
import com.shield.module.meeting.repository.MeetingAgendaRepository;
import com.shield.module.meeting.repository.MeetingAttendeeRepository;
import com.shield.module.meeting.repository.MeetingMinutesRecordRepository;
import com.shield.module.meeting.repository.MeetingReminderRepository;
import com.shield.module.meeting.repository.MeetingRepository;
import com.shield.module.meeting.repository.MeetingResolutionRepository;
import com.shield.module.meeting.repository.MeetingVoteRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MeetingAgendaRepository meetingAgendaRepository;

    @Mock
    private MeetingAttendeeRepository meetingAttendeeRepository;

    @Mock
    private MeetingMinutesRecordRepository meetingMinutesRecordRepository;

    @Mock
    private MeetingResolutionRepository meetingResolutionRepository;

    @Mock
    private MeetingVoteRepository meetingVoteRepository;

    @Mock
    private MeetingActionItemRepository meetingActionItemRepository;

    @Mock
    private MeetingReminderRepository meetingReminderRepository;

    @Mock
    private AuditLogService auditLogService;

    private MeetingService meetingService;

    @BeforeEach
    void setUp() {
        meetingService = new MeetingService(
                meetingRepository,
                meetingAgendaRepository,
                meetingAttendeeRepository,
                meetingMinutesRecordRepository,
                meetingResolutionRepository,
                meetingVoteRepository,
                meetingActionItemRepository,
                meetingReminderRepository,
                auditLogService);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createShouldSetScheduledStatus() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(meetingRepository.save(any(MeetingEntity.class))).thenAnswer(invocation -> {
            MeetingEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new ShieldPrincipal(userId, tenantId, "admin@shield.dev", "ADMIN"),
                null));

        MeetingResponse response = meetingService.create(new MeetingCreateRequest(
                "AGM",
                "Annual meeting",
                "Agenda",
                Instant.now().plusSeconds(3600),
                "Club House",
                "IN_PERSON",
                null));

        assertEquals(MeetingStatus.SCHEDULED, response.status());
        assertEquals(tenantId, response.tenantId());
        assertEquals(userId, response.createdBy());
    }

    @Test
    void updateMinutesShouldMarkMeetingCompleted() {
        UUID meetingId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        MeetingEntity meeting = new MeetingEntity();
        meeting.setId(meetingId);
        meeting.setTenantId(tenantId);
        meeting.setStatus(MeetingStatus.SCHEDULED);

        when(meetingRepository.findByIdAndDeletedFalse(meetingId)).thenReturn(Optional.of(meeting));
        when(meetingRepository.save(any(MeetingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(meetingMinutesRecordRepository.findFirstByMeetingIdAndDeletedFalseOrderByCreatedAtDesc(meetingId))
                .thenReturn(Optional.empty());

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new ShieldPrincipal(userId, tenantId, "admin@shield.dev", "ADMIN"),
                null));

        MeetingResponse response = meetingService.updateMinutes(meetingId, new MeetingMinutesUpdateRequest("Minutes text"));

        assertEquals(MeetingStatus.COMPLETED, response.status());
        assertEquals("Minutes text", response.minutes());
    }

    @Test
    void voteShouldPersistVoteAndRefreshCounts() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID resolutionId = UUID.randomUUID();

        MeetingResolutionEntity resolution = new MeetingResolutionEntity();
        resolution.setId(resolutionId);
        resolution.setTenantId(tenantId);
        resolution.setStatus(MeetingResolutionStatus.DEFERRED);

        when(meetingResolutionRepository.findByIdAndDeletedFalse(resolutionId)).thenReturn(Optional.of(resolution));
        when(meetingVoteRepository.findFirstByResolutionIdAndUserIdAndDeletedFalse(resolutionId, userId)).thenReturn(Optional.empty());
        when(meetingVoteRepository.save(any(MeetingVoteEntity.class))).thenAnswer(invocation -> {
            MeetingVoteEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return entity;
        });
        when(meetingVoteRepository.countByResolutionIdAndVoteAndDeletedFalse(resolutionId, MeetingVoteChoice.FOR)).thenReturn(3L);
        when(meetingVoteRepository.countByResolutionIdAndVoteAndDeletedFalse(resolutionId, MeetingVoteChoice.AGAINST)).thenReturn(1L);
        when(meetingVoteRepository.countByResolutionIdAndVoteAndDeletedFalse(resolutionId, MeetingVoteChoice.ABSTAIN)).thenReturn(1L);
        when(meetingResolutionRepository.save(any(MeetingResolutionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new ShieldPrincipal(userId, tenantId, "admin@shield.dev", "ADMIN"),
                null));

        MeetingVoteResponse response = meetingService.vote(resolutionId, new MeetingVoteRequest(null, MeetingVoteChoice.FOR));

        assertEquals(MeetingVoteChoice.FOR, response.vote());
        assertEquals(userId, response.userId());
        assertEquals(3, resolution.getVotesFor());
        assertEquals(1, resolution.getVotesAgainst());
        assertEquals(1, resolution.getVotesAbstain());
    }

    @Test
    void attendanceReportShouldAggregateStatuses() {
        UUID meetingId = UUID.randomUUID();

        MeetingEntity meeting = new MeetingEntity();
        meeting.setId(meetingId);

        MeetingAttendeeEntity attendee1 = new MeetingAttendeeEntity();
        attendee1.setRsvpStatus(MeetingAttendeeRsvpStatus.ACCEPTED);
        attendee1.setAttendanceStatus(MeetingAttendeeAttendanceStatus.PRESENT);

        MeetingAttendeeEntity attendee2 = new MeetingAttendeeEntity();
        attendee2.setRsvpStatus(MeetingAttendeeRsvpStatus.DECLINED);
        attendee2.setAttendanceStatus(MeetingAttendeeAttendanceStatus.ABSENT);

        MeetingAttendeeEntity attendee3 = new MeetingAttendeeEntity();
        attendee3.setRsvpStatus(MeetingAttendeeRsvpStatus.PENDING);

        when(meetingRepository.findByIdAndDeletedFalse(meetingId)).thenReturn(Optional.of(meeting));
        when(meetingAttendeeRepository.findAllByMeetingIdAndDeletedFalseOrderByCreatedAtAsc(meetingId))
                .thenReturn(List.of(attendee1, attendee2, attendee3));

        MeetingAttendanceReportResponse response = meetingService.attendanceReport(meetingId);

        assertEquals(3, response.totalAttendees());
        assertEquals(1, response.accepted());
        assertEquals(1, response.declined());
        assertEquals(1, response.pending());
        assertEquals(1, response.present());
        assertEquals(1, response.absent());
    }

    @Test
    void getShouldThrowWhenMeetingMissing() {
        UUID meetingId = UUID.randomUUID();
        when(meetingRepository.findByIdAndDeletedFalse(meetingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> meetingService.get(meetingId));
    }
}
