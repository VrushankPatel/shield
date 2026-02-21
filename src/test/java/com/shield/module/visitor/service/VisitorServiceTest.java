package com.shield.module.visitor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.module.visitor.dto.BlacklistCheckResponse;
import com.shield.module.visitor.dto.DomesticHelpAssignUnitRequest;
import com.shield.module.visitor.dto.VisitorLogEntryRequest;
import com.shield.module.visitor.dto.VisitorLogExitRequest;
import com.shield.module.visitor.dto.VisitorPassCreateRequest;
import com.shield.module.visitor.dto.VisitorPassPreApproveRequest;
import com.shield.module.visitor.entity.BlacklistEntity;
import com.shield.module.visitor.entity.DomesticHelpEntity;
import com.shield.module.visitor.entity.DomesticHelpUnitMappingEntity;
import com.shield.module.visitor.entity.VisitorEntity;
import com.shield.module.visitor.entity.VisitorEntryExitLogEntity;
import com.shield.module.visitor.entity.VisitorPassEntity;
import com.shield.module.visitor.entity.VisitorPassStatus;
import com.shield.module.visitor.repository.BlacklistRepository;
import com.shield.module.visitor.repository.DeliveryLogRepository;
import com.shield.module.visitor.repository.DomesticHelpRepository;
import com.shield.module.visitor.repository.DomesticHelpUnitMappingRepository;
import com.shield.module.visitor.repository.VisitorEntryExitLogRepository;
import com.shield.module.visitor.repository.VisitorPassRepository;
import com.shield.module.visitor.repository.VisitorRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VisitorServiceTest {

    @Mock
    private VisitorPassRepository visitorPassRepository;
    @Mock
    private VisitorRepository visitorRepository;
    @Mock
    private VisitorEntryExitLogRepository visitorEntryExitLogRepository;
    @Mock
    private DomesticHelpRepository domesticHelpRepository;
    @Mock
    private DomesticHelpUnitMappingRepository domesticHelpUnitMappingRepository;
    @Mock
    private BlacklistRepository blacklistRepository;
    @Mock
    private DeliveryLogRepository deliveryLogRepository;
    @Mock
    private AuditLogService auditLogService;

    private VisitorService visitorService;

    @BeforeEach
    void setUp() {
        visitorService = new VisitorService(
                visitorPassRepository,
                visitorRepository,
                visitorEntryExitLogRepository,
                domesticHelpRepository,
                domesticHelpUnitMappingRepository,
                blacklistRepository,
                deliveryLogRepository,
                auditLogService);
    }

    @Test
    void createPassShouldUseVisitorFallbackAndPendingStatus() {
        ShieldPrincipal principal = principal();
        UUID visitorId = UUID.randomUUID();

        VisitorEntity visitor = new VisitorEntity();
        visitor.setId(visitorId);
        visitor.setTenantId(principal.tenantId());
        visitor.setVisitorName("Courier Person");
        visitor.setVehicleNumber("MH12AB1234");

        when(visitorRepository.findByIdAndDeletedFalse(visitorId)).thenReturn(Optional.of(visitor));
        when(visitorPassRepository.existsByPassNumber(any())).thenReturn(false);
        when(visitorPassRepository.save(any(VisitorPassEntity.class))).thenAnswer(invocation -> {
            VisitorPassEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = visitorService.createPass(new VisitorPassCreateRequest(
                visitorId,
                UUID.randomUUID(),
                null,
                null,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                null,
                "Package delivery",
                null), principal);

        assertNotNull(response.id());
        assertEquals("Courier Person", response.visitorName());
        assertEquals("MH12AB1234", response.vehicleNumber());
        assertEquals(VisitorPassStatus.PENDING, response.status());
        assertEquals(1, response.numberOfPersons());
        assertTrue(response.passNumber().startsWith("VP-"));
    }

    @Test
    void preApproveShouldSetActiveStatusAndApprovedBy() {
        ShieldPrincipal principal = principal();

        when(visitorPassRepository.existsByPassNumber(any())).thenReturn(false);
        when(visitorPassRepository.save(any(VisitorPassEntity.class))).thenAnswer(invocation -> {
            VisitorPassEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = visitorService.preApprovePass(new VisitorPassPreApproveRequest(
                null,
                UUID.randomUUID(),
                "Guest",
                null,
                Instant.now(),
                Instant.now().plusSeconds(7200),
                LocalDate.now(),
                "Dinner",
                3,
                null), principal);

        assertEquals(VisitorPassStatus.ACTIVE, response.status());
        assertEquals(principal.userId(), response.approvedBy());
        assertEquals(3, response.numberOfPersons());
    }

    @Test
    void verifyPassByQrCodeShouldMarkExpiredWhenPastValidity() {
        VisitorPassEntity pass = new VisitorPassEntity();
        pass.setId(UUID.randomUUID());
        pass.setTenantId(UUID.randomUUID());
        pass.setUnitId(UUID.randomUUID());
        pass.setVisitorName("Expired Guest");
        pass.setValidFrom(Instant.now().minusSeconds(7200));
        pass.setValidTo(Instant.now().minusSeconds(60));
        pass.setStatus(VisitorPassStatus.ACTIVE);
        pass.setQrCode("VIS-test-qr");
        pass.setNumberOfPersons(1);

        when(visitorPassRepository.findByQrCodeAndDeletedFalse("VIS-test-qr")).thenReturn(Optional.of(pass));
        when(visitorPassRepository.save(any(VisitorPassEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = visitorService.verifyPassByQrCode("VIS-test-qr");

        assertEquals(VisitorPassStatus.EXPIRED, response.status());
    }

    @Test
    void logEntryAndExitShouldUpdatePassLifecycle() {
        ShieldPrincipal principal = principal();
        UUID passId = UUID.randomUUID();

        VisitorPassEntity pass = new VisitorPassEntity();
        pass.setId(passId);
        pass.setTenantId(principal.tenantId());
        pass.setUnitId(UUID.randomUUID());
        pass.setVisitorName("Guest");
        pass.setValidFrom(Instant.now().minusSeconds(300));
        pass.setValidTo(Instant.now().plusSeconds(3600));
        pass.setStatus(VisitorPassStatus.APPROVED);
        pass.setNumberOfPersons(1);

        when(visitorPassRepository.findByIdAndDeletedFalse(passId)).thenReturn(Optional.of(pass));
        when(visitorPassRepository.save(any(VisitorPassEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(visitorEntryExitLogRepository.save(any(VisitorEntryExitLogEntity.class))).thenAnswer(invocation -> {
            VisitorEntryExitLogEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return entity;
        });

        var entry = visitorService.logEntry(new VisitorLogEntryRequest(passId, "Gate-1", null, null), principal);
        assertEquals(passId, entry.visitorPassId());
        assertEquals(VisitorPassStatus.ACTIVE, pass.getStatus());

        VisitorEntryExitLogEntity openLog = new VisitorEntryExitLogEntity();
        openLog.setId(entry.id());
        openLog.setTenantId(principal.tenantId());
        openLog.setVisitorPassId(passId);
        openLog.setEntryTime(Instant.now().minusSeconds(120));

        when(visitorEntryExitLogRepository.findFirstByVisitorPassIdAndExitTimeIsNullAndDeletedFalseOrderByEntryTimeDesc(passId))
                .thenReturn(Optional.of(openLog));
        when(visitorPassRepository.findByIdAndDeletedFalse(passId)).thenReturn(Optional.of(pass));

        var exit = visitorService.logExit(new VisitorLogExitRequest(passId, "Gate-2", null), principal);
        assertNotNull(exit.exitTime());
        assertEquals(VisitorPassStatus.USED, pass.getStatus());
    }

    @Test
    void checkBlacklistByPhoneShouldReturnBlacklistedWhenActiveEntryExists() {
        BlacklistEntity entity = new BlacklistEntity();
        entity.setId(UUID.randomUUID());
        entity.setTenantId(UUID.randomUUID());
        entity.setPhone("9999999999");
        entity.setActive(true);

        when(blacklistRepository.findByPhoneAndActiveTrueAndDeletedFalse("9999999999")).thenReturn(Optional.of(entity));

        BlacklistCheckResponse response = visitorService.checkBlacklistByPhone("9999999999");

        assertEquals("9999999999", response.phone());
        assertTrue(response.blacklisted());
    }

    @Test
    void assignDomesticHelpToUnitShouldPersistActiveMapping() {
        ShieldPrincipal principal = principal();
        UUID helpId = UUID.randomUUID();

        DomesticHelpEntity help = new DomesticHelpEntity();
        help.setId(helpId);
        help.setTenantId(principal.tenantId());
        help.setHelpName("Maid Asha");

        when(domesticHelpRepository.findByIdAndDeletedFalse(helpId)).thenReturn(Optional.of(help));
        when(domesticHelpUnitMappingRepository.save(any(DomesticHelpUnitMappingEntity.class))).thenAnswer(invocation -> {
            DomesticHelpUnitMappingEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = visitorService.assignDomesticHelpToUnit(
                helpId,
                new DomesticHelpAssignUnitRequest(UUID.randomUUID(), null, null),
                principal);

        assertNotNull(response.id());
        assertTrue(response.active());
        assertNotNull(response.startDate());
    }

    private ShieldPrincipal principal() {
        return new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
    }
}
