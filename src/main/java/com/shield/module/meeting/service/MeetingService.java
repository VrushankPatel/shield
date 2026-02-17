package com.shield.module.meeting.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.meeting.dto.MeetingCreateRequest;
import com.shield.module.meeting.dto.MeetingMinutesUpdateRequest;
import com.shield.module.meeting.dto.MeetingResponse;
import com.shield.module.meeting.entity.MeetingEntity;
import com.shield.module.meeting.entity.MeetingStatus;
import com.shield.module.meeting.repository.MeetingRepository;
import com.shield.tenant.context.TenantContext;
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
    private final AuditLogService auditLogService;

    public MeetingResponse create(MeetingCreateRequest request) {
        UUID tenantId = TenantContext.getRequiredTenantId();

        MeetingEntity entity = new MeetingEntity();
        entity.setTenantId(tenantId);
        entity.setTitle(request.title());
        entity.setAgenda(request.agenda());
        entity.setScheduledAt(request.scheduledAt());
        entity.setStatus(MeetingStatus.SCHEDULED);

        MeetingEntity saved = meetingRepository.save(entity);
        auditLogService.record(tenantId, null, "MEETING_CREATED", "meeting", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<MeetingResponse> list(Pageable pageable) {
        return PagedResponse.from(meetingRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public MeetingResponse updateMinutes(UUID id, MeetingMinutesUpdateRequest request) {
        MeetingEntity entity = meetingRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found: " + id));

        entity.setMinutes(request.minutes());
        entity.setStatus(MeetingStatus.COMPLETED);
        MeetingEntity saved = meetingRepository.save(entity);
        auditLogService.record(saved.getTenantId(), null, "MEETING_MINUTES_UPDATED", "meeting", saved.getId(), null);
        return toResponse(saved);
    }

    private MeetingResponse toResponse(MeetingEntity entity) {
        return new MeetingResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getTitle(),
                entity.getAgenda(),
                entity.getScheduledAt(),
                entity.getMinutes(),
                entity.getStatus());
    }
}
