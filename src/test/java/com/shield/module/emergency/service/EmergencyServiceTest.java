package com.shield.module.emergency.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.module.emergency.dto.SosAlertRaiseRequest;
import com.shield.module.emergency.dto.SosAlertResponse;
import com.shield.module.emergency.entity.SosAlertEntity;
import com.shield.module.emergency.entity.SosAlertStatus;
import com.shield.module.emergency.repository.EmergencyContactRepository;
import com.shield.module.emergency.repository.FireDrillRecordRepository;
import com.shield.module.emergency.repository.SafetyEquipmentRepository;
import com.shield.module.emergency.repository.SafetyInspectionRepository;
import com.shield.module.emergency.repository.SosAlertRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmergencyServiceTest {

    @Mock
    private EmergencyContactRepository emergencyContactRepository;

    @Mock
    private SosAlertRepository sosAlertRepository;

    @Mock
    private FireDrillRecordRepository fireDrillRecordRepository;

    @Mock
    private SafetyEquipmentRepository safetyEquipmentRepository;

    @Mock
    private SafetyInspectionRepository safetyInspectionRepository;

    @Mock
    private AuditLogService auditLogService;

    private EmergencyService emergencyService;

    @BeforeEach
    void setUp() {
        emergencyService = new EmergencyService(
                emergencyContactRepository,
                sosAlertRepository,
                fireDrillRecordRepository,
                safetyEquipmentRepository,
                safetyInspectionRepository,
                auditLogService);
    }

    @Test
    void raiseAlertShouldCreateActiveAlert() {
        when(sosAlertRepository.save(any(SosAlertEntity.class))).thenAnswer(invocation -> {
            SosAlertEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "owner@shield.dev", "OWNER");
        SosAlertRaiseRequest request = new SosAlertRaiseRequest(
                UUID.randomUUID(),
                "MEDICAL",
                "Tower A",
                "Need immediate support",
                null,
                null);

        SosAlertResponse response = emergencyService.raiseAlert(request, principal);

        assertEquals("ACTIVE", response.status().name());
        assertEquals(principal.userId(), response.raisedBy());
        assertEquals(principal.tenantId(), response.tenantId());
    }

    @Test
    void markFalseAlarmShouldUpdateStatus() {
        UUID alertId = UUID.randomUUID();
        SosAlertEntity alert = new SosAlertEntity();
        alert.setId(alertId);
        alert.setStatus(SosAlertStatus.ACTIVE);

        when(sosAlertRepository.findByIdAndDeletedFalse(alertId)).thenReturn(Optional.of(alert));
        when(sosAlertRepository.save(any(SosAlertEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "security@shield.dev", "SECURITY");
        SosAlertResponse response = emergencyService.markFalseAlarm(alertId, principal);

        assertEquals(SosAlertStatus.FALSE_ALARM, response.status());
    }

    @Test
    void listAlertsByDateRangeShouldValidateRange() {
        assertThrows(BadRequestException.class, () -> emergencyService.listAlertsByDateRange(
                Instant.now(),
                Instant.now().minusSeconds(60),
                org.springframework.data.domain.PageRequest.of(0, 10)));
    }
}
