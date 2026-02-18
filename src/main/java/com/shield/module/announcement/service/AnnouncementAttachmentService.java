package com.shield.module.announcement.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.announcement.dto.AnnouncementAttachmentRequest;
import com.shield.module.announcement.dto.AnnouncementAttachmentResponse;
import com.shield.module.announcement.entity.AnnouncementAttachmentEntity;
import com.shield.module.announcement.repository.AnnouncementAttachmentRepository;
import com.shield.module.announcement.repository.AnnouncementRepository;
import com.shield.security.model.ShieldPrincipal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AnnouncementAttachmentService {

    private final AnnouncementAttachmentRepository attachmentRepository;
    private final AnnouncementRepository announcementRepository;
    private final AuditLogService auditLogService;

    public AnnouncementAttachmentService(
            AnnouncementAttachmentRepository attachmentRepository,
            AnnouncementRepository announcementRepository,
            AuditLogService auditLogService) {
        this.attachmentRepository = attachmentRepository;
        this.announcementRepository = announcementRepository;
        this.auditLogService = auditLogService;
    }

    public AnnouncementAttachmentResponse addAttachment(UUID announcementId, AnnouncementAttachmentRequest request,
            ShieldPrincipal principal) {
        announcementRepository.findByIdAndDeletedFalse(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + announcementId));

        AnnouncementAttachmentEntity entity = new AnnouncementAttachmentEntity();
        entity.setTenantId(principal.tenantId());
        entity.setAnnouncementId(announcementId);
        entity.setFileName(request.fileName());
        entity.setFileUrl(request.fileUrl());
        entity.setFileSize(request.fileSize());
        entity.setContentType(request.contentType());

        AnnouncementAttachmentEntity saved = attachmentRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "ATTACHMENT_ADDED", "announcement_attachment",
                saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementAttachmentResponse> listAttachments(UUID announcementId) {
        return attachmentRepository.findAllByAnnouncementIdAndDeletedFalse(announcementId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteAttachment(UUID attachmentId, ShieldPrincipal principal) {
        AnnouncementAttachmentEntity entity = attachmentRepository.findByIdAndDeletedFalse(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));

        entity.setDeleted(true);
        attachmentRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "ATTACHMENT_DELETED",
                "announcement_attachment", attachmentId, null);
    }

    private AnnouncementAttachmentResponse toResponse(AnnouncementAttachmentEntity entity) {
        return new AnnouncementAttachmentResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getAnnouncementId(),
                entity.getFileName(),
                entity.getFileUrl(),
                entity.getFileSize(),
                entity.getContentType(),
                entity.getCreatedAt());
    }
}
