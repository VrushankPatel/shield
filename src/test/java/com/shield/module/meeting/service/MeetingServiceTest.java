package com.shield.module.meeting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.meeting.dto.MeetingCreateRequest;
import com.shield.module.meeting.dto.MeetingMinutesUpdateRequest;
import com.shield.module.meeting.dto.MeetingResponse;
import com.shield.module.meeting.entity.MeetingEntity;
import com.shield.module.meeting.entity.MeetingStatus;
import com.shield.module.meeting.repository.MeetingRepository;
import com.shield.tenant.context.TenantContext;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private AuditLogService auditLogService;

    private MeetingService meetingService;

    @BeforeEach
    void setUp() {
        meetingService = new MeetingService(meetingRepository, auditLogService);
    }

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void createShouldSetScheduledStatus() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        when(meetingRepository.save(any(MeetingEntity.class))).thenAnswer(invocation -> {
            MeetingEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        MeetingResponse response = meetingService.create(new MeetingCreateRequest(
                "AGM",
                "Annual review",
                Instant.now().plusSeconds(3600)));

        assertEquals(tenantId, response.tenantId());
        assertEquals(MeetingStatus.SCHEDULED, response.status());
    }

    @Test
    void updateMinutesShouldMarkCompleted() {
        UUID meetingId = UUID.randomUUID();

        MeetingEntity meeting = new MeetingEntity();
        meeting.setId(meetingId);
        meeting.setTenantId(UUID.randomUUID());
        meeting.setStatus(MeetingStatus.SCHEDULED);

        when(meetingRepository.findByIdAndDeletedFalse(meetingId)).thenReturn(Optional.of(meeting));
        when(meetingRepository.save(any(MeetingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MeetingResponse response = meetingService.updateMinutes(meetingId, new MeetingMinutesUpdateRequest("Minutes text"));

        assertEquals(MeetingStatus.COMPLETED, response.status());
        assertEquals("Minutes text", response.minutes());
    }

    @Test
    void updateMinutesShouldThrowWhenMissing() {
        UUID meetingId = UUID.randomUUID();
        when(meetingRepository.findByIdAndDeletedFalse(meetingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> meetingService.updateMinutes(meetingId, new MeetingMinutesUpdateRequest("x")));
    }
}
