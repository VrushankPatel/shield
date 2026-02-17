package com.shield.module.emergency.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.module.emergency.dto.SosAlertRaiseRequest;
import com.shield.module.emergency.dto.SosAlertResponse;
import com.shield.module.emergency.entity.SosAlertEntity;
import com.shield.module.emergency.repository.EmergencyContactRepository;
import com.shield.module.emergency.repository.SosAlertRepository;
import com.shield.security.model.ShieldPrincipal;
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
    private AuditLogService auditLogService;

    private EmergencyService emergencyService;

    @BeforeEach
    void setUp() {
        emergencyService = new EmergencyService(emergencyContactRepository, sosAlertRepository, auditLogService);
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
}
