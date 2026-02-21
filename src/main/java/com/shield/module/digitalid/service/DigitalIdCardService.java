package com.shield.module.digitalid.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.common.exception.UnauthorizedException;
import com.shield.module.digitalid.dto.DigitalIdCardResponse;
import com.shield.module.digitalid.dto.DigitalIdGenerateRequest;
import com.shield.module.digitalid.dto.DigitalIdRenewRequest;
import com.shield.module.digitalid.dto.DigitalIdVerificationResponse;
import com.shield.module.digitalid.entity.DigitalIdCardEntity;
import com.shield.module.digitalid.repository.DigitalIdCardRepository;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DigitalIdCardService {

    private static final int DEFAULT_VALIDITY_DAYS = 365;
    private static final String ENTITY_DIGITAL_ID_CARD = "digital_id_card";

    private final DigitalIdCardRepository digitalIdCardRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public DigitalIdCardService(
            DigitalIdCardRepository digitalIdCardRepository,
            UserRepository userRepository,
            AuditLogService auditLogService) {
        this.digitalIdCardRepository = digitalIdCardRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PagedResponse<DigitalIdCardResponse> listByUser(UUID userId, Pageable pageable, ShieldPrincipal principal) {
        enforceSelfOrPrivileged(principal, userId);
        return PagedResponse.from(digitalIdCardRepository.findAllByUserIdAndDeletedFalse(userId, pageable).map(this::toResponse));
    }

    public DigitalIdCardResponse generate(DigitalIdGenerateRequest request, ShieldPrincipal principal) {
        userRepository.findByIdAndDeletedFalse(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.userId()));

        DigitalIdCardEntity entity = new DigitalIdCardEntity();
        entity.setTenantId(principal.tenantId());
        entity.setUserId(request.userId());
        entity.setQrCodeData(generateQrCodeData());
        entity.setQrCodeUrl(request.qrCodeUrl());

        LocalDate issueDate = LocalDate.now();
        int validityDays = request.validityDays() == null ? DEFAULT_VALIDITY_DAYS : request.validityDays();
        entity.setIssueDate(issueDate);
        entity.setExpiryDate(issueDate.plusDays(validityDays));
        entity.setActive(true);
        entity.setDeactivatedAt(null);

        DigitalIdCardEntity saved = digitalIdCardRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DIGITAL_ID_GENERATED", ENTITY_DIGITAL_ID_CARD, saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DigitalIdCardResponse getById(UUID id, ShieldPrincipal principal) {
        DigitalIdCardEntity entity = findEntity(id);
        enforceSelfOrPrivileged(principal, entity.getUserId());
        return toResponse(entity);
    }

    public DigitalIdCardResponse renew(UUID id, DigitalIdRenewRequest request, ShieldPrincipal principal) {
        DigitalIdCardEntity entity = findEntity(id);

        int validityDays = request.validityDays() == null ? DEFAULT_VALIDITY_DAYS : request.validityDays();
        LocalDate issueDate = LocalDate.now();
        entity.setIssueDate(issueDate);
        entity.setExpiryDate(issueDate.plusDays(validityDays));
        entity.setActive(true);
        entity.setDeactivatedAt(null);
        entity.setQrCodeData(generateQrCodeData());
        if (request.qrCodeUrl() != null) {
            entity.setQrCodeUrl(request.qrCodeUrl());
        }

        DigitalIdCardEntity saved = digitalIdCardRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DIGITAL_ID_RENEWED", ENTITY_DIGITAL_ID_CARD, saved.getId(), null);
        return toResponse(saved);
    }

    public DigitalIdCardResponse deactivate(UUID id, ShieldPrincipal principal) {
        DigitalIdCardEntity entity = findEntity(id);
        if (!entity.isActive()) {
            return toResponse(entity);
        }

        entity.setActive(false);
        entity.setDeactivatedAt(Instant.now());
        DigitalIdCardEntity saved = digitalIdCardRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DIGITAL_ID_DEACTIVATED", ENTITY_DIGITAL_ID_CARD, saved.getId(), null);
        return toResponse(saved);
    }

    public DigitalIdVerificationResponse verifyByQrCode(String qrCode, ShieldPrincipal principal) {
        DigitalIdCardEntity entity = digitalIdCardRepository.findByQrCodeDataAndDeletedFalse(qrCode)
                .orElseThrow(() -> new ResourceNotFoundException("Digital ID card not found"));

        LocalDate today = LocalDate.now();
        boolean expired = entity.getExpiryDate().isBefore(today);
        if (expired && entity.isActive()) {
            entity.setActive(false);
            entity.setDeactivatedAt(Instant.now());
            digitalIdCardRepository.save(entity);
        }

        boolean valid = entity.isActive() && !expired;
        String message = buildVerificationMessage(valid, expired);

        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DIGITAL_ID_VERIFIED", ENTITY_DIGITAL_ID_CARD, entity.getId(), null);
        return new DigitalIdVerificationResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getQrCodeData(),
                entity.isActive(),
                expired,
                valid,
                entity.getExpiryDate(),
                message);
    }

    private DigitalIdCardEntity findEntity(UUID id) {
        return digitalIdCardRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Digital ID card not found: " + id));
    }

    private void enforceSelfOrPrivileged(ShieldPrincipal principal, UUID targetUserId) {
        if (isPrivileged(principal)) {
            return;
        }
        if (!principal.userId().equals(targetUserId)) {
            throw new UnauthorizedException("You are not allowed to access this digital ID card");
        }
    }

    private boolean isPrivileged(ShieldPrincipal principal) {
        return "ADMIN".equals(principal.role()) || "COMMITTEE".equals(principal.role()) || "SECURITY".equals(principal.role());
    }

    private String generateQrCodeData() {
        return "SID-" + UUID.randomUUID();
    }

    private String buildVerificationMessage(boolean valid, boolean expired) {
        if (valid) {
            return "Card is valid";
        }
        if (expired) {
            return "Card has expired";
        }
        return "Card is inactive";
    }

    private DigitalIdCardResponse toResponse(DigitalIdCardEntity entity) {
        return new DigitalIdCardResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getUserId(),
                entity.getQrCodeData(),
                entity.getQrCodeUrl(),
                entity.getIssueDate(),
                entity.getExpiryDate(),
                entity.isActive(),
                entity.getDeactivatedAt());
    }
}
