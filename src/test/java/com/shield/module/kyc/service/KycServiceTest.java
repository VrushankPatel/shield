package com.shield.module.kyc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.UnauthorizedException;
import com.shield.module.kyc.dto.KycDocumentCreateRequest;
import com.shield.module.kyc.dto.KycDocumentResponse;
import com.shield.module.kyc.entity.KycDocumentEntity;
import com.shield.module.kyc.entity.KycDocumentType;
import com.shield.module.kyc.entity.KycVerificationStatus;
import com.shield.module.kyc.repository.KycDocumentRepository;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.model.ShieldPrincipal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KycServiceTest {

    @Mock
    private KycDocumentRepository kycDocumentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    private KycService kycService;

    @BeforeEach
    void setUp() {
        kycService = new KycService(kycDocumentRepository, userRepository, auditLogService);
    }

    @Test
    void uploadShouldRejectWhenResidentUploadsForAnotherUser() {
        UUID tenantId = UUID.randomUUID();
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), tenantId, "resident@shield.dev", "TENANT");

        KycDocumentCreateRequest request = new KycDocumentCreateRequest(
                UUID.randomUUID(),
                KycDocumentType.PAN,
                "ABCDE1234F",
                "https://files.example/pan.pdf");

        assertThrows(UnauthorizedException.class, () -> kycService.upload(request, principal));
    }

    @Test
    void verifyShouldMarkDocumentVerified() {
        UUID tenantId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();

        KycDocumentEntity entity = new KycDocumentEntity();
        entity.setId(documentId);
        entity.setTenantId(tenantId);
        entity.setUserId(userId);
        entity.setDocumentType(KycDocumentType.AADHAAR);
        entity.setDocumentNumber("123412341234");
        entity.setVerificationStatus(KycVerificationStatus.PENDING);

        when(kycDocumentRepository.findByIdAndDeletedFalse(documentId)).thenReturn(Optional.of(entity));
        when(kycDocumentRepository.save(any(KycDocumentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(reviewerId, tenantId, "admin@shield.dev", "ADMIN");
        KycDocumentResponse response = kycService.verify(documentId, principal);

        assertEquals(KycVerificationStatus.VERIFIED, response.verificationStatus());
        assertEquals(reviewerId, response.verifiedBy());
        verify(auditLogService).record(eq(tenantId), eq(reviewerId), eq("KYC_VERIFIED"), eq("kyc_document"), eq(documentId), any());
    }
}
