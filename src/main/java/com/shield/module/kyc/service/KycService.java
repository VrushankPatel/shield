package com.shield.module.kyc.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.common.exception.UnauthorizedException;
import com.shield.module.kyc.dto.KycDecisionRequest;
import com.shield.module.kyc.dto.KycDocumentCreateRequest;
import com.shield.module.kyc.dto.KycDocumentResponse;
import com.shield.module.kyc.dto.KycDocumentUpdateRequest;
import com.shield.module.kyc.entity.KycDocumentEntity;
import com.shield.module.kyc.entity.KycVerificationStatus;
import com.shield.module.kyc.repository.KycDocumentRepository;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class KycService {

    private static final String ENTITY_KYC_DOCUMENT = "kyc_document";

    private final KycDocumentRepository kycDocumentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public KycService(
            KycDocumentRepository kycDocumentRepository,
            UserRepository userRepository,
            AuditLogService auditLogService) {
        this.kycDocumentRepository = kycDocumentRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public KycDocumentResponse upload(KycDocumentCreateRequest request, ShieldPrincipal principal) {
        enforceSelfOrPrivileged(principal, request.userId());

        UserEntity user = userRepository.findByIdAndDeletedFalse(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.userId()));

        KycDocumentEntity entity = new KycDocumentEntity();
        entity.setTenantId(principal.tenantId());
        entity.setUserId(user.getId());
        entity.setDocumentType(request.documentType());
        entity.setDocumentNumber(request.documentNumber());
        entity.setDocumentUrl(request.documentUrl());
        entity.setVerificationStatus(KycVerificationStatus.PENDING);

        KycDocumentEntity saved = kycDocumentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "KYC_UPLOADED", ENTITY_KYC_DOCUMENT, saved.getId(), null);
        return toResponse(saved);
    }

    public KycDocumentResponse update(UUID id, KycDocumentUpdateRequest request, ShieldPrincipal principal) {
        KycDocumentEntity entity = findEntity(id);
        enforceSelfOrPrivileged(principal, entity.getUserId());

        if (entity.getVerificationStatus() == KycVerificationStatus.VERIFIED) {
            throw new BadRequestException("Verified KYC document cannot be modified");
        }

        entity.setDocumentType(request.documentType());
        entity.setDocumentNumber(request.documentNumber());
        entity.setDocumentUrl(request.documentUrl());
        entity.setVerificationStatus(KycVerificationStatus.PENDING);
        entity.setRejectionReason(null);
        entity.setVerifiedAt(null);
        entity.setVerifiedBy(null);

        KycDocumentEntity saved = kycDocumentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "KYC_UPDATED", ENTITY_KYC_DOCUMENT, saved.getId(), null);
        return toResponse(saved);
    }

    public void delete(UUID id, ShieldPrincipal principal) {
        KycDocumentEntity entity = findEntity(id);
        enforceSelfOrPrivileged(principal, entity.getUserId());

        entity.setDeleted(true);
        kycDocumentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "KYC_DELETED", ENTITY_KYC_DOCUMENT, entity.getId(), null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<KycDocumentResponse> listByUser(UUID userId, Pageable pageable, ShieldPrincipal principal) {
        enforceSelfOrPrivileged(principal, userId);
        return PagedResponse.from(kycDocumentRepository.findAllByUserIdAndDeletedFalse(userId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<KycDocumentResponse> listPending(Pageable pageable) {
        return PagedResponse.from(kycDocumentRepository
                .findAllByVerificationStatusAndDeletedFalse(KycVerificationStatus.PENDING, pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public KycDocumentResponse getById(UUID id, ShieldPrincipal principal) {
        KycDocumentEntity entity = findEntity(id);
        enforceSelfOrPrivileged(principal, entity.getUserId());
        return toResponse(entity);
    }

    public KycDocumentResponse verify(UUID id, ShieldPrincipal principal) {
        KycDocumentEntity entity = findEntity(id);
        entity.setVerificationStatus(KycVerificationStatus.VERIFIED);
        entity.setRejectionReason(null);
        entity.setVerifiedAt(Instant.now());
        entity.setVerifiedBy(principal.userId());

        KycDocumentEntity saved = kycDocumentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "KYC_VERIFIED", ENTITY_KYC_DOCUMENT, saved.getId(), null);
        return toResponse(saved);
    }

    public KycDocumentResponse reject(UUID id, KycDecisionRequest request, ShieldPrincipal principal) {
        KycDocumentEntity entity = findEntity(id);
        entity.setVerificationStatus(KycVerificationStatus.REJECTED);
        entity.setRejectionReason(request.rejectionReason());
        entity.setVerifiedAt(Instant.now());
        entity.setVerifiedBy(principal.userId());

        KycDocumentEntity saved = kycDocumentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "KYC_REJECTED", ENTITY_KYC_DOCUMENT, saved.getId(), null);
        return toResponse(saved);
    }

    private KycDocumentEntity findEntity(UUID id) {
        return kycDocumentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("KYC document not found: " + id));
    }

    private void enforceSelfOrPrivileged(ShieldPrincipal principal, UUID targetUserId) {
        if (isPrivileged(principal)) {
            return;
        }
        if (!principal.userId().equals(targetUserId)) {
            throw new UnauthorizedException("You are not allowed to access this KYC record");
        }
    }

    private boolean isPrivileged(ShieldPrincipal principal) {
        return "ADMIN".equals(principal.role()) || "COMMITTEE".equals(principal.role());
    }

    private KycDocumentResponse toResponse(KycDocumentEntity entity) {
        return new KycDocumentResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getUserId(),
                entity.getDocumentType(),
                entity.getDocumentNumber(),
                entity.getDocumentUrl(),
                entity.getVerificationStatus(),
                entity.getRejectionReason(),
                entity.getVerifiedAt(),
                entity.getVerifiedBy(),
                entity.getCreatedAt());
    }
}
