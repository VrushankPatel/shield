package com.shield.module.meeting.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.common.util.SecurityUtils;
import com.shield.module.meeting.dto.MeetingActionItemCreateRequest;
import com.shield.module.meeting.dto.MeetingActionItemResponse;
import com.shield.module.meeting.dto.MeetingActionItemUpdateRequest;
import com.shield.module.meeting.dto.MeetingAgendaCreateRequest;
import com.shield.module.meeting.dto.MeetingAgendaOrderRequest;
import com.shield.module.meeting.dto.MeetingAgendaResponse;
import com.shield.module.meeting.dto.MeetingAgendaUpdateRequest;
import com.shield.module.meeting.dto.MeetingAttendanceMarkRequest;
import com.shield.module.meeting.dto.MeetingAttendanceReportResponse;
import com.shield.module.meeting.dto.MeetingAttendeeCreateRequest;
import com.shield.module.meeting.dto.MeetingAttendeeResponse;
import com.shield.module.meeting.dto.MeetingAttendeeRsvpRequest;
import com.shield.module.meeting.dto.MeetingCreateRequest;
import com.shield.module.meeting.dto.MeetingMinutesApproveRequest;
import com.shield.module.meeting.dto.MeetingMinutesCreateRequest;
import com.shield.module.meeting.dto.MeetingMinutesResponse;
import com.shield.module.meeting.dto.MeetingMinutesUpdateRequest;
import com.shield.module.meeting.dto.MeetingReminderResponse;
import com.shield.module.meeting.dto.MeetingResolutionCreateRequest;
import com.shield.module.meeting.dto.MeetingResolutionResponse;
import com.shield.module.meeting.dto.MeetingResolutionUpdateRequest;
import com.shield.module.meeting.dto.MeetingResponse;
import com.shield.module.meeting.dto.MeetingUpdateRequest;
import com.shield.module.meeting.dto.MeetingVoteRequest;
import com.shield.module.meeting.dto.MeetingVoteResponse;
import com.shield.module.meeting.dto.MeetingVoteResultResponse;
import com.shield.module.meeting.entity.MeetingActionItemEntity;
import com.shield.module.meeting.entity.MeetingActionItemStatus;
import com.shield.module.meeting.entity.MeetingAgendaEntity;
import com.shield.module.meeting.entity.MeetingAttendeeAttendanceStatus;
import com.shield.module.meeting.entity.MeetingAttendeeEntity;
import com.shield.module.meeting.entity.MeetingAttendeeRsvpStatus;
import com.shield.module.meeting.entity.MeetingEntity;
import com.shield.module.meeting.entity.MeetingMinutesRecordEntity;
import com.shield.module.meeting.entity.MeetingReminderEntity;
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
import com.shield.tenant.context.TenantContext;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingAgendaRepository meetingAgendaRepository;
    private final MeetingAttendeeRepository meetingAttendeeRepository;
    private final MeetingMinutesRecordRepository meetingMinutesRecordRepository;
    private final MeetingResolutionRepository meetingResolutionRepository;
    private final MeetingVoteRepository meetingVoteRepository;
    private final MeetingActionItemRepository meetingActionItemRepository;
    private final MeetingReminderRepository meetingReminderRepository;
    private final AuditLogService auditLogService;

    public MeetingResponse create(MeetingCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();

        MeetingEntity entity = new MeetingEntity();
        entity.setTenantId(principal.tenantId());
        entity.setMeetingNumber(generateMeetingNumber());
        entity.setMeetingType(request.meetingType());
        entity.setTitle(request.title());
        entity.setAgenda(request.agenda());
        entity.setScheduledAt(request.scheduledAt());
        entity.setLocation(request.location());
        entity.setMeetingMode(request.meetingMode());
        entity.setMeetingLink(request.meetingLink());
        entity.setCreatedBy(principal.userId());
        entity.setStatus(MeetingStatus.SCHEDULED);

        MeetingEntity saved = meetingRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "MEETING_CREATED", "meeting", saved.getId(), null);
        return toMeetingResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MeetingResponse> list(Pageable pageable) {
        return PagedResponse.from(meetingRepository.findAllByDeletedFalse(pageable).map(this::toMeetingResponse));
    }

    @Transactional(readOnly = true)
    public MeetingResponse get(UUID id) {
        return toMeetingResponse(getMeetingEntity(id));
    }

    public MeetingResponse update(UUID id, MeetingUpdateRequest request) {
        MeetingEntity entity = getMeetingEntity(id);
        entity.setMeetingType(request.meetingType());
        entity.setTitle(request.title());
        entity.setAgenda(request.agenda());
        entity.setScheduledAt(request.scheduledAt());
        entity.setLocation(request.location());
        entity.setMeetingMode(request.meetingMode());
        entity.setMeetingLink(request.meetingLink());
        if (request.status() != null) {
            entity.setStatus(request.status());
        }

        MeetingEntity saved = meetingRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "MEETING_UPDATED", "meeting", saved.getId(), null);
        return toMeetingResponse(saved);
    }

    public void delete(UUID id) {
        MeetingEntity entity = getMeetingEntity(id);
        entity.setDeleted(true);
        meetingRepository.save(entity);
        auditLogService.record(entity.getTenantId(), null, "MEETING_DELETED", "meeting", entity.getId(), null);
    }

    public MeetingResponse start(UUID id) {
        MeetingEntity entity = getMeetingEntity(id);
        entity.setStatus(MeetingStatus.ONGOING);
        MeetingEntity saved = meetingRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "MEETING_STARTED", "meeting", saved.getId(), null);
        return toMeetingResponse(saved);
    }

    public MeetingResponse end(UUID id) {
        MeetingEntity entity = getMeetingEntity(id);
        entity.setStatus(MeetingStatus.COMPLETED);
        MeetingEntity saved = meetingRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "MEETING_ENDED", "meeting", saved.getId(), null);
        return toMeetingResponse(saved);
    }

    public MeetingResponse cancel(UUID id) {
        MeetingEntity entity = getMeetingEntity(id);
        entity.setStatus(MeetingStatus.CANCELLED);
        MeetingEntity saved = meetingRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "MEETING_CANCELLED", "meeting", saved.getId(), null);
        return toMeetingResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MeetingResponse> listUpcoming(Pageable pageable) {
        return PagedResponse.from(
                meetingRepository.findAllByScheduledAtGreaterThanEqualAndDeletedFalse(Instant.now(), pageable)
                        .map(this::toMeetingResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<MeetingResponse> listPast(Pageable pageable) {
        return PagedResponse.from(
                meetingRepository.findAllByScheduledAtLessThanAndDeletedFalse(Instant.now(), pageable)
                        .map(this::toMeetingResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<MeetingResponse> listByType(String type, Pageable pageable) {
        return PagedResponse.from(
                meetingRepository.findAllByMeetingTypeIgnoreCaseAndDeletedFalse(type, pageable)
                        .map(this::toMeetingResponse));
    }

    @Transactional(readOnly = true)
    public List<MeetingAgendaResponse> listAgenda(UUID meetingId) {
        getMeetingEntity(meetingId);
        return meetingAgendaRepository.findAllByMeetingIdAndDeletedFalseOrderByDisplayOrderAscCreatedAtAsc(meetingId)
                .stream()
                .map(this::toMeetingAgendaResponse)
                .toList();
    }

    public MeetingAgendaResponse createAgenda(UUID meetingId, MeetingAgendaCreateRequest request) {
        MeetingEntity meeting = getMeetingEntity(meetingId);

        MeetingAgendaEntity entity = new MeetingAgendaEntity();
        entity.setTenantId(meeting.getTenantId());
        entity.setMeetingId(meetingId);
        entity.setAgendaItem(request.agendaItem());
        entity.setDescription(request.description());
        entity.setDisplayOrder(request.displayOrder());
        entity.setPresenter(request.presenter());
        entity.setEstimatedDuration(request.estimatedDuration());

        MeetingAgendaEntity saved = meetingAgendaRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "MEETING_AGENDA_CREATED", "meeting_agenda", saved.getId(), null);
        return toMeetingAgendaResponse(saved);
    }

    public MeetingAgendaResponse updateAgenda(UUID agendaId, MeetingAgendaUpdateRequest request) {
        MeetingAgendaEntity entity = getMeetingAgendaEntity(agendaId);
        entity.setAgendaItem(request.agendaItem());
        entity.setDescription(request.description());
        entity.setDisplayOrder(request.displayOrder());
        entity.setPresenter(request.presenter());
        entity.setEstimatedDuration(request.estimatedDuration());

        MeetingAgendaEntity saved = meetingAgendaRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "MEETING_AGENDA_UPDATED", "meeting_agenda", saved.getId(), null);
        return toMeetingAgendaResponse(saved);
    }

    public MeetingAgendaResponse reorderAgenda(UUID agendaId, MeetingAgendaOrderRequest request) {
        MeetingAgendaEntity entity = getMeetingAgendaEntity(agendaId);
        entity.setDisplayOrder(request.displayOrder());

        MeetingAgendaEntity saved = meetingAgendaRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "MEETING_AGENDA_REORDERED", "meeting_agenda", saved.getId(), null);
        return toMeetingAgendaResponse(saved);
    }

    public void deleteAgenda(UUID agendaId) {
        MeetingAgendaEntity entity = getMeetingAgendaEntity(agendaId);
        entity.setDeleted(true);
        meetingAgendaRepository.save(entity);
        auditLogService.record(entity.getTenantId(), null, "MEETING_AGENDA_DELETED", "meeting_agenda", entity.getId(), null);
    }

    @Transactional(readOnly = true)
    public List<MeetingAttendeeResponse> listAttendees(UUID meetingId) {
        getMeetingEntity(meetingId);
        return meetingAttendeeRepository.findAllByMeetingIdAndDeletedFalseOrderByCreatedAtAsc(meetingId)
                .stream()
                .map(this::toMeetingAttendeeResponse)
                .toList();
    }

    public MeetingAttendeeResponse addAttendee(UUID meetingId, MeetingAttendeeCreateRequest request) {
        MeetingEntity meeting = getMeetingEntity(meetingId);
        MeetingAttendeeEntity existing = meetingAttendeeRepository
                .findFirstByMeetingIdAndUserIdAndDeletedFalse(meetingId, request.userId())
                .orElse(null);

        if (existing != null) {
            return toMeetingAttendeeResponse(existing);
        }

        MeetingAttendeeEntity entity = new MeetingAttendeeEntity();
        entity.setTenantId(meeting.getTenantId());
        entity.setMeetingId(meetingId);
        entity.setUserId(request.userId());
        entity.setInvitationSentAt(Instant.now());
        entity.setRsvpStatus(MeetingAttendeeRsvpStatus.PENDING);

        MeetingAttendeeEntity saved = meetingAttendeeRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "MEETING_ATTENDEE_ADDED", "meeting_attendee", saved.getId(), null);
        return toMeetingAttendeeResponse(saved);
    }

    public void removeAttendee(UUID meetingId, UUID userId) {
        getMeetingEntity(meetingId);
        MeetingAttendeeEntity attendee = meetingAttendeeRepository
                .findFirstByMeetingIdAndUserIdAndDeletedFalse(meetingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting attendee not found for user: " + userId));

        attendee.setDeleted(true);
        meetingAttendeeRepository.save(attendee);
        auditLogService.record(attendee.getTenantId(), null, "MEETING_ATTENDEE_REMOVED", "meeting_attendee", attendee.getId(), null);
    }

    public MeetingAttendeeResponse rsvp(UUID meetingId, MeetingAttendeeRsvpRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        UUID userId = request.userId() != null ? request.userId() : principal.userId();

        getMeetingEntity(meetingId);
        MeetingAttendeeEntity attendee = meetingAttendeeRepository
                .findFirstByMeetingIdAndUserIdAndDeletedFalse(meetingId, userId)
                .orElseGet(() -> {
                    MeetingAttendeeEntity created = new MeetingAttendeeEntity();
                    created.setTenantId(principal.tenantId());
                    created.setMeetingId(meetingId);
                    created.setUserId(userId);
                    created.setInvitationSentAt(Instant.now());
                    created.setRsvpStatus(MeetingAttendeeRsvpStatus.PENDING);
                    return created;
                });

        attendee.setRsvpStatus(request.rsvpStatus());
        MeetingAttendeeEntity saved = meetingAttendeeRepository.save(attendee);
        auditLogService.record(saved.getTenantId(), principal.userId(), "MEETING_RSVP_UPDATED", "meeting_attendee", saved.getId(), null);
        return toMeetingAttendeeResponse(saved);
    }

    public MeetingAttendeeResponse markAttendance(UUID meetingId, MeetingAttendanceMarkRequest request) {
        getMeetingEntity(meetingId);
        MeetingAttendeeEntity attendee = meetingAttendeeRepository
                .findFirstByMeetingIdAndUserIdAndDeletedFalse(meetingId, request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Meeting attendee not found for user: " + request.userId()));

        attendee.setAttendanceStatus(request.attendanceStatus());
        attendee.setJoinedAt(request.joinedAt() != null ? request.joinedAt() : attendee.getJoinedAt());
        attendee.setLeftAt(request.leftAt() != null ? request.leftAt() : attendee.getLeftAt());

        MeetingAttendeeEntity saved = meetingAttendeeRepository.save(attendee);
        auditLogService.record(saved.getTenantId(), null, "MEETING_ATTENDANCE_MARKED", "meeting_attendee", saved.getId(), null);
        return toMeetingAttendeeResponse(saved);
    }

    @Transactional(readOnly = true)
    public MeetingAttendanceReportResponse attendanceReport(UUID meetingId) {
        getMeetingEntity(meetingId);
        List<MeetingAttendeeEntity> attendees = meetingAttendeeRepository.findAllByMeetingIdAndDeletedFalseOrderByCreatedAtAsc(meetingId);

        long accepted = attendees.stream().filter(a -> a.getRsvpStatus() == MeetingAttendeeRsvpStatus.ACCEPTED).count();
        long declined = attendees.stream().filter(a -> a.getRsvpStatus() == MeetingAttendeeRsvpStatus.DECLINED).count();
        long pending = attendees.stream().filter(a -> a.getRsvpStatus() == MeetingAttendeeRsvpStatus.PENDING).count();
        long present = attendees.stream().filter(a -> a.getAttendanceStatus() == MeetingAttendeeAttendanceStatus.PRESENT).count();
        long absent = attendees.stream().filter(a -> a.getAttendanceStatus() == MeetingAttendeeAttendanceStatus.ABSENT).count();

        return new MeetingAttendanceReportResponse(meetingId, attendees.size(), accepted, declined, pending, present, absent);
    }

    @Transactional(readOnly = true)
    public MeetingMinutesResponse getMinutesByMeeting(UUID meetingId) {
        getMeetingEntity(meetingId);
        MeetingMinutesRecordEntity minutes = meetingMinutesRecordRepository
                .findFirstByMeetingIdAndDeletedFalseOrderByCreatedAtDesc(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting minutes not found for meeting: " + meetingId));
        return toMeetingMinutesResponse(minutes);
    }

    public MeetingMinutesResponse createMinutes(UUID meetingId, MeetingMinutesCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        MeetingEntity meeting = getMeetingEntity(meetingId);

        MeetingMinutesRecordEntity entity = new MeetingMinutesRecordEntity();
        entity.setTenantId(meeting.getTenantId());
        entity.setMeetingId(meetingId);
        entity.setMinutesContent(request.minutesContent());
        entity.setSummary(request.summary());
        entity.setPreparedBy(request.preparedBy() != null ? request.preparedBy() : principal.userId());
        entity.setDocumentUrl(request.documentUrl());

        meeting.setMinutes(request.minutesContent());
        meetingRepository.save(meeting);

        MeetingMinutesRecordEntity saved = meetingMinutesRecordRepository.save(entity);
        auditLogService.record(saved.getTenantId(), principal.userId(), "MEETING_MINUTES_CREATED", "meeting_minutes_record", saved.getId(), null);
        return toMeetingMinutesResponse(saved);
    }

    public MeetingMinutesResponse updateMinutes(UUID minutesId, MeetingMinutesCreateRequest request) {
        MeetingMinutesRecordEntity entity = getMeetingMinutesEntity(minutesId);
        entity.setMinutesContent(request.minutesContent());
        entity.setSummary(request.summary());
        entity.setPreparedBy(request.preparedBy());
        entity.setDocumentUrl(request.documentUrl());

        MeetingMinutesRecordEntity saved = meetingMinutesRecordRepository.save(entity);
        MeetingEntity meeting = getMeetingEntity(saved.getMeetingId());
        meeting.setMinutes(saved.getMinutesContent());
        meetingRepository.save(meeting);

        auditLogService.record(saved.getTenantId(), null, "MEETING_MINUTES_UPDATED", "meeting_minutes_record", saved.getId(), null);
        return toMeetingMinutesResponse(saved);
    }

    public MeetingResponse updateMinutes(UUID meetingId, MeetingMinutesUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        MeetingEntity meeting = getMeetingEntity(meetingId);
        meeting.setMinutes(request.minutes());
        meeting.setStatus(MeetingStatus.COMPLETED);
        MeetingEntity savedMeeting = meetingRepository.save(meeting);

        MeetingMinutesRecordEntity minutes = meetingMinutesRecordRepository
                .findFirstByMeetingIdAndDeletedFalseOrderByCreatedAtDesc(meetingId)
                .orElseGet(() -> {
                    MeetingMinutesRecordEntity created = new MeetingMinutesRecordEntity();
                    created.setTenantId(principal.tenantId());
                    created.setMeetingId(meetingId);
                    return created;
                });

        minutes.setMinutesContent(request.minutes());
        minutes.setPreparedBy(principal.userId());
        meetingMinutesRecordRepository.save(minutes);

        auditLogService.record(savedMeeting.getTenantId(), principal.userId(), "MEETING_MINUTES_UPDATED", "meeting", savedMeeting.getId(), null);
        return toMeetingResponse(savedMeeting);
    }

    public MeetingMinutesResponse approveMinutes(UUID minutesId, MeetingMinutesApproveRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        MeetingMinutesRecordEntity entity = getMeetingMinutesEntity(minutesId);
        entity.setApprovedBy(request.approvedBy() != null ? request.approvedBy() : principal.userId());
        entity.setApprovalDate(LocalDate.now());

        MeetingMinutesRecordEntity saved = meetingMinutesRecordRepository.save(entity);
        auditLogService.record(saved.getTenantId(), principal.userId(), "MEETING_MINUTES_APPROVED", "meeting_minutes_record", saved.getId(), null);
        return toMeetingMinutesResponse(saved);
    }

    @Transactional(readOnly = true)
    public Map<String, String> downloadMinutes(UUID minutesId) {
        MeetingMinutesRecordEntity entity = getMeetingMinutesEntity(minutesId);
        return Map.of(
                "minutesId", entity.getId().toString(),
                "documentUrl", entity.getDocumentUrl() == null ? "NOT_AVAILABLE" : entity.getDocumentUrl());
    }

    public MeetingMinutesResponse generateAiSummary(UUID minutesId) {
        MeetingMinutesRecordEntity entity = getMeetingMinutesEntity(minutesId);
        String base = entity.getSummary() != null && !entity.getSummary().isBlank()
                ? entity.getSummary()
                : entity.getMinutesContent();
        String aiSummary = base.length() > 300 ? base.substring(0, 300) + "..." : base;
        entity.setAiGeneratedSummary("AI summary placeholder: " + aiSummary);

        MeetingMinutesRecordEntity saved = meetingMinutesRecordRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "MEETING_MINUTES_AI_SUMMARY_GENERATED", "meeting_minutes_record", saved.getId(), null);
        return toMeetingMinutesResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MeetingResolutionResponse> listResolutions(UUID meetingId) {
        getMeetingEntity(meetingId);
        return meetingResolutionRepository.findAllByMeetingIdAndDeletedFalseOrderByCreatedAtAsc(meetingId)
                .stream()
                .map(this::toMeetingResolutionResponse)
                .toList();
    }

    public MeetingResolutionResponse createResolution(UUID meetingId, MeetingResolutionCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        MeetingEntity meeting = getMeetingEntity(meetingId);

        MeetingResolutionEntity entity = new MeetingResolutionEntity();
        entity.setTenantId(meeting.getTenantId());
        entity.setMeetingId(meetingId);
        entity.setResolutionNumber(generateResolutionNumber());
        entity.setResolutionText(request.resolutionText());
        entity.setProposedBy(request.proposedBy() != null ? request.proposedBy() : principal.userId());
        entity.setSecondedBy(request.secondedBy());
        entity.setStatus(MeetingResolutionStatus.DEFERRED);
        entity.setVotesFor(0);
        entity.setVotesAgainst(0);
        entity.setVotesAbstain(0);

        MeetingResolutionEntity saved = meetingResolutionRepository.save(entity);
        auditLogService.record(saved.getTenantId(), principal.userId(), "MEETING_RESOLUTION_CREATED", "meeting_resolution", saved.getId(), null);
        return toMeetingResolutionResponse(saved);
    }

    public MeetingResolutionResponse updateResolution(UUID id, MeetingResolutionUpdateRequest request) {
        MeetingResolutionEntity entity = getMeetingResolutionEntity(id);
        entity.setResolutionText(request.resolutionText());
        entity.setProposedBy(request.proposedBy());
        entity.setSecondedBy(request.secondedBy());
        if (request.status() != null) {
            entity.setStatus(request.status());
        }

        MeetingResolutionEntity saved = meetingResolutionRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "MEETING_RESOLUTION_UPDATED", "meeting_resolution", saved.getId(), null);
        return toMeetingResolutionResponse(saved);
    }

    public void deleteResolution(UUID id) {
        MeetingResolutionEntity entity = getMeetingResolutionEntity(id);
        entity.setDeleted(true);
        meetingResolutionRepository.save(entity);
        auditLogService.record(entity.getTenantId(), null, "MEETING_RESOLUTION_DELETED", "meeting_resolution", entity.getId(), null);
    }

    @Transactional(readOnly = true)
    public MeetingResolutionResponse getResolution(UUID id) {
        return toMeetingResolutionResponse(getMeetingResolutionEntity(id));
    }

    @Transactional(readOnly = true)
    public List<MeetingResolutionResponse> listResolutionsByStatus(MeetingResolutionStatus status) {
        return meetingResolutionRepository.findAllByStatusAndDeletedFalse(status)
                .stream()
                .map(this::toMeetingResolutionResponse)
                .toList();
    }

    public MeetingVoteResponse vote(UUID resolutionId, MeetingVoteRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        MeetingResolutionEntity resolution = getMeetingResolutionEntity(resolutionId);
        UUID voterId = request.userId() != null ? request.userId() : principal.userId();

        MeetingVoteEntity vote = meetingVoteRepository
                .findFirstByResolutionIdAndUserIdAndDeletedFalse(resolutionId, voterId)
                .orElseGet(() -> {
                    MeetingVoteEntity created = new MeetingVoteEntity();
                    created.setTenantId(resolution.getTenantId());
                    created.setResolutionId(resolutionId);
                    created.setUserId(voterId);
                    return created;
                });

        vote.setVote(request.vote());
        vote.setVotedAt(Instant.now());

        MeetingVoteEntity savedVote = meetingVoteRepository.save(vote);
        refreshResolutionVoteCounts(resolution);
        auditLogService.record(savedVote.getTenantId(), principal.userId(), "MEETING_RESOLUTION_VOTED", "meeting_vote", savedVote.getId(), null);
        return toMeetingVoteResponse(savedVote);
    }

    @Transactional(readOnly = true)
    public List<MeetingVoteResponse> listVotes(UUID resolutionId) {
        getMeetingResolutionEntity(resolutionId);
        return meetingVoteRepository.findAllByResolutionIdAndDeletedFalseOrderByCreatedAtAsc(resolutionId)
                .stream()
                .map(this::toMeetingVoteResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MeetingVoteResultResponse voteResults(UUID resolutionId) {
        MeetingResolutionEntity resolution = getMeetingResolutionEntity(resolutionId);
        int votesFor = intCount(meetingVoteRepository.countByResolutionIdAndVoteAndDeletedFalse(resolutionId, MeetingVoteChoice.FOR));
        int votesAgainst = intCount(meetingVoteRepository.countByResolutionIdAndVoteAndDeletedFalse(resolutionId, MeetingVoteChoice.AGAINST));
        int votesAbstain = intCount(meetingVoteRepository.countByResolutionIdAndVoteAndDeletedFalse(resolutionId, MeetingVoteChoice.ABSTAIN));
        int totalVotes = votesFor + votesAgainst + votesAbstain;
        return new MeetingVoteResultResponse(resolutionId, votesFor, votesAgainst, votesAbstain, totalVotes, resolution.getStatus());
    }

    @Transactional(readOnly = true)
    public MeetingVoteResponse myVote(UUID resolutionId) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        MeetingVoteEntity vote = meetingVoteRepository
                .findFirstByResolutionIdAndUserIdAndDeletedFalse(resolutionId, principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Vote not found for resolution: " + resolutionId));
        return toMeetingVoteResponse(vote);
    }

    @Transactional(readOnly = true)
    public List<MeetingActionItemResponse> listActionItems(UUID meetingId) {
        getMeetingEntity(meetingId);
        return meetingActionItemRepository.findAllByMeetingIdAndDeletedFalseOrderByCreatedAtAsc(meetingId)
                .stream()
                .map(this::toMeetingActionItemResponse)
                .toList();
    }

    public MeetingActionItemResponse createActionItem(UUID meetingId, MeetingActionItemCreateRequest request) {
        MeetingEntity meeting = getMeetingEntity(meetingId);

        MeetingActionItemEntity entity = new MeetingActionItemEntity();
        entity.setTenantId(meeting.getTenantId());
        entity.setMeetingId(meetingId);
        entity.setActionDescription(request.actionDescription());
        entity.setAssignedTo(request.assignedTo());
        entity.setDueDate(request.dueDate());
        entity.setPriority(request.priority());
        entity.setStatus(request.status() != null ? request.status() : MeetingActionItemStatus.PENDING);

        MeetingActionItemEntity saved = meetingActionItemRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "MEETING_ACTION_ITEM_CREATED", "meeting_action_item", saved.getId(), null);
        return toMeetingActionItemResponse(saved);
    }

    public MeetingActionItemResponse updateActionItem(UUID id, MeetingActionItemUpdateRequest request) {
        MeetingActionItemEntity entity = getMeetingActionItemEntity(id);
        entity.setActionDescription(request.actionDescription());
        entity.setAssignedTo(request.assignedTo());
        entity.setDueDate(request.dueDate());
        entity.setPriority(request.priority());
        entity.setStatus(request.status() != null ? request.status() : entity.getStatus());
        entity.setCompletionDate(request.completionDate());

        MeetingActionItemEntity saved = meetingActionItemRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "MEETING_ACTION_ITEM_UPDATED", "meeting_action_item", saved.getId(), null);
        return toMeetingActionItemResponse(saved);
    }

    public void deleteActionItem(UUID id) {
        MeetingActionItemEntity entity = getMeetingActionItemEntity(id);
        entity.setDeleted(true);
        meetingActionItemRepository.save(entity);
        auditLogService.record(entity.getTenantId(), null, "MEETING_ACTION_ITEM_DELETED", "meeting_action_item", entity.getId(), null);
    }

    public MeetingActionItemResponse completeActionItem(UUID id) {
        MeetingActionItemEntity entity = getMeetingActionItemEntity(id);
        entity.setStatus(MeetingActionItemStatus.COMPLETED);
        entity.setCompletionDate(LocalDate.now());

        MeetingActionItemEntity saved = meetingActionItemRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "MEETING_ACTION_ITEM_COMPLETED", "meeting_action_item", saved.getId(), null);
        return toMeetingActionItemResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MeetingActionItemResponse> listAssignedToMe(Pageable pageable) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return PagedResponse.from(
                meetingActionItemRepository.findAllByAssignedToAndDeletedFalse(principal.userId(), pageable)
                        .map(this::toMeetingActionItemResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<MeetingActionItemResponse> listPendingActionItems(Pageable pageable) {
        return PagedResponse.from(
                meetingActionItemRepository.findAllByStatusAndDeletedFalse(MeetingActionItemStatus.PENDING, pageable)
                        .map(this::toMeetingActionItemResponse));
    }

    public MeetingReminderResponse sendReminder(UUID meetingId, String reminderType) {
        MeetingEntity meeting = getMeetingEntity(meetingId);

        MeetingReminderEntity entity = new MeetingReminderEntity();
        entity.setTenantId(meeting.getTenantId());
        entity.setMeetingId(meetingId);
        entity.setReminderType(reminderType == null || reminderType.isBlank() ? "MANUAL" : reminderType);
        entity.setSentAt(Instant.now());

        MeetingReminderEntity saved = meetingReminderRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "MEETING_REMINDER_SENT", "meeting_reminder", saved.getId(), null);
        return toMeetingReminderResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MeetingReminderResponse> listReminders(UUID meetingId) {
        getMeetingEntity(meetingId);
        return meetingReminderRepository.findAllByMeetingIdAndDeletedFalseOrderBySentAtDesc(meetingId)
                .stream()
                .map(this::toMeetingReminderResponse)
                .toList();
    }

    private MeetingEntity getMeetingEntity(UUID id) {
        return meetingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found: " + id));
    }

    private MeetingAgendaEntity getMeetingAgendaEntity(UUID id) {
        return meetingAgendaRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting agenda not found: " + id));
    }

    private MeetingMinutesRecordEntity getMeetingMinutesEntity(UUID id) {
        return meetingMinutesRecordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting minutes not found: " + id));
    }

    private MeetingResolutionEntity getMeetingResolutionEntity(UUID id) {
        return meetingResolutionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting resolution not found: " + id));
    }

    private MeetingActionItemEntity getMeetingActionItemEntity(UUID id) {
        return meetingActionItemRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting action item not found: " + id));
    }

    private void refreshResolutionVoteCounts(MeetingResolutionEntity resolution) {
        int votesFor = intCount(meetingVoteRepository.countByResolutionIdAndVoteAndDeletedFalse(resolution.getId(), MeetingVoteChoice.FOR));
        int votesAgainst = intCount(meetingVoteRepository.countByResolutionIdAndVoteAndDeletedFalse(resolution.getId(), MeetingVoteChoice.AGAINST));
        int votesAbstain = intCount(meetingVoteRepository.countByResolutionIdAndVoteAndDeletedFalse(resolution.getId(), MeetingVoteChoice.ABSTAIN));

        resolution.setVotesFor(votesFor);
        resolution.setVotesAgainst(votesAgainst);
        resolution.setVotesAbstain(votesAbstain);

        meetingResolutionRepository.save(resolution);
    }

    private int intCount(long value) {
        return (int) Math.min(value, Integer.MAX_VALUE);
    }

    private String generateMeetingNumber() {
        return "MTG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateResolutionNumber() {
        return "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private MeetingResponse toMeetingResponse(MeetingEntity entity) {
        return new MeetingResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getMeetingNumber(),
                entity.getMeetingType(),
                entity.getTitle(),
                entity.getAgenda(),
                entity.getScheduledAt(),
                entity.getLocation(),
                entity.getMeetingMode(),
                entity.getMeetingLink(),
                entity.getCreatedBy(),
                entity.getMinutes(),
                entity.getStatus());
    }

    private MeetingAgendaResponse toMeetingAgendaResponse(MeetingAgendaEntity entity) {
        return new MeetingAgendaResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getMeetingId(),
                entity.getAgendaItem(),
                entity.getDescription(),
                entity.getDisplayOrder(),
                entity.getPresenter(),
                entity.getEstimatedDuration());
    }

    private MeetingAttendeeResponse toMeetingAttendeeResponse(MeetingAttendeeEntity entity) {
        return new MeetingAttendeeResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getMeetingId(),
                entity.getUserId(),
                entity.getInvitationSentAt(),
                entity.getRsvpStatus(),
                entity.getAttendanceStatus(),
                entity.getJoinedAt(),
                entity.getLeftAt());
    }

    private MeetingMinutesResponse toMeetingMinutesResponse(MeetingMinutesRecordEntity entity) {
        return new MeetingMinutesResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getMeetingId(),
                entity.getMinutesContent(),
                entity.getSummary(),
                entity.getAiGeneratedSummary(),
                entity.getPreparedBy(),
                entity.getApprovedBy(),
                entity.getApprovalDate(),
                entity.getDocumentUrl());
    }

    private MeetingResolutionResponse toMeetingResolutionResponse(MeetingResolutionEntity entity) {
        return new MeetingResolutionResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getMeetingId(),
                entity.getResolutionNumber(),
                entity.getResolutionText(),
                entity.getProposedBy(),
                entity.getSecondedBy(),
                entity.getStatus(),
                entity.getVotesFor(),
                entity.getVotesAgainst(),
                entity.getVotesAbstain());
    }

    private MeetingVoteResponse toMeetingVoteResponse(MeetingVoteEntity entity) {
        return new MeetingVoteResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getResolutionId(),
                entity.getUserId(),
                entity.getVote(),
                entity.getVotedAt());
    }

    private MeetingActionItemResponse toMeetingActionItemResponse(MeetingActionItemEntity entity) {
        return new MeetingActionItemResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getMeetingId(),
                entity.getActionDescription(),
                entity.getAssignedTo(),
                entity.getDueDate(),
                entity.getPriority(),
                entity.getStatus(),
                entity.getCompletionDate());
    }

    private MeetingReminderResponse toMeetingReminderResponse(MeetingReminderEntity entity) {
        return new MeetingReminderResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getMeetingId(),
                entity.getReminderType(),
                entity.getSentAt());
    }
}
