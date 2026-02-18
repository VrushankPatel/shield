package com.shield.module.digitalid.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.module.digitalid.dto.DigitalIdCardResponse;
import com.shield.module.digitalid.dto.DigitalIdGenerateRequest;
import com.shield.module.digitalid.dto.DigitalIdRenewRequest;
import com.shield.module.digitalid.dto.DigitalIdVerificationResponse;
import com.shield.module.digitalid.entity.DigitalIdCardEntity;
import com.shield.module.digitalid.repository.DigitalIdCardRepository;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DigitalIdCardServiceTest {

    @Mock
    private DigitalIdCardRepository digitalIdCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    private DigitalIdCardService digitalIdCardService;

    @BeforeEach
    void setUp() {
        digitalIdCardService = new DigitalIdCardService(digitalIdCardRepository, userRepository, auditLogService);
    }

    @Test
    void generateShouldCreateActiveCard() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserEntity user = new UserEntity();
        user.setId(userId);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(digitalIdCardRepository.save(any(DigitalIdCardEntity.class))).thenAnswer(invocation -> {
            DigitalIdCardEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), tenantId, "admin@shield.dev", "ADMIN");
        DigitalIdCardResponse response = digitalIdCardService.generate(
                new DigitalIdGenerateRequest(userId, 365, "https://qr.png"),
                principal);

        assertEquals(userId, response.userId());
        assertEquals(true, response.active());
    }

    @Test
    void renewShouldReactivateCard() {
        UUID tenantId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        DigitalIdCardEntity entity = new DigitalIdCardEntity();
        entity.setId(cardId);
        entity.setTenantId(tenantId);
        entity.setUserId(UUID.randomUUID());
        entity.setActive(false);
        entity.setIssueDate(LocalDate.now().minusDays(200));
        entity.setExpiryDate(LocalDate.now().minusDays(10));

        when(digitalIdCardRepository.findByIdAndDeletedFalse(cardId)).thenReturn(Optional.of(entity));
        when(digitalIdCardRepository.save(any(DigitalIdCardEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), tenantId, "security@shield.dev", "SECURITY");
        DigitalIdCardResponse response = digitalIdCardService.renew(cardId, new DigitalIdRenewRequest(100, null), principal);

        assertEquals(true, response.active());
        assertEquals(LocalDate.now().plusDays(100), response.expiryDate());
    }

    @Test
    void verifyByQrCodeShouldMarkExpiredCardInvalid() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        DigitalIdCardEntity entity = new DigitalIdCardEntity();
        entity.setId(cardId);
        entity.setTenantId(tenantId);
        entity.setUserId(userId);
        entity.setQrCodeData("SID-EXPIRED");
        entity.setIssueDate(LocalDate.now().minusDays(400));
        entity.setExpiryDate(LocalDate.now().minusDays(1));
        entity.setActive(true);

        when(digitalIdCardRepository.findByQrCodeDataAndDeletedFalse("SID-EXPIRED")).thenReturn(Optional.of(entity));
        when(digitalIdCardRepository.save(any(DigitalIdCardEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), tenantId, "security@shield.dev", "SECURITY");
        DigitalIdVerificationResponse response = digitalIdCardService.verifyByQrCode("SID-EXPIRED", principal);

        assertEquals(false, response.valid());
        assertEquals(true, response.expired());
        assertEquals(false, response.active());
        verify(auditLogService).record(eq(tenantId), eq(principal.userId()), eq("DIGITAL_ID_VERIFIED"), eq("digital_id_card"), eq(cardId), any());
    }
}
