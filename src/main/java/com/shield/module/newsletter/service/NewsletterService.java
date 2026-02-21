package com.shield.module.newsletter.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.newsletter.dto.NewsletterCreateRequest;
import com.shield.module.newsletter.dto.NewsletterResponse;
import com.shield.module.newsletter.dto.NewsletterUpdateRequest;
import com.shield.module.newsletter.entity.NewsletterEntity;
import com.shield.module.newsletter.entity.NewsletterStatus;
import com.shield.module.newsletter.repository.NewsletterRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NewsletterService {

    private static final String ENTITY_NEWSLETTER = "newsletter";

    private final NewsletterRepository newsletterRepository;
    private final AuditLogService auditLogService;

    public NewsletterService(NewsletterRepository newsletterRepository, AuditLogService auditLogService) {
        this.newsletterRepository = newsletterRepository;
        this.auditLogService = auditLogService;
    }

    public NewsletterResponse create(NewsletterCreateRequest request, ShieldPrincipal principal) {
        NewsletterEntity entity = new NewsletterEntity();
        entity.setTenantId(principal.tenantId());
        entity.setTitle(request.title());
        entity.setContent(request.content());
        entity.setSummary(request.summary());
        entity.setFileUrl(request.fileUrl());
        entity.setYear(request.year());
        entity.setMonth(request.month());
        entity.setStatus(NewsletterStatus.DRAFT);
        entity.setCreatedBy(principal.userId());

        NewsletterEntity saved = newsletterRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "NEWSLETTER_CREATED", ENTITY_NEWSLETTER,
                saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public NewsletterResponse getById(UUID id) {
        return toResponse(findNewsletter(id));
    }

    @Transactional(readOnly = true)
    public PagedResponse<NewsletterResponse> list(Pageable pageable) {
        return PagedResponse.from(newsletterRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<NewsletterResponse> listByYear(int year, Pageable pageable) {
        return PagedResponse
                .from(newsletterRepository.findAllByYearAndDeletedFalse(year, pageable).map(this::toResponse));
    }

    public NewsletterResponse update(UUID id, NewsletterUpdateRequest request, ShieldPrincipal principal) {
        NewsletterEntity entity = findNewsletter(id);
        if (entity.getStatus() != NewsletterStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT newsletters can be updated");
        }
        entity.setTitle(request.title());
        entity.setContent(request.content());
        entity.setSummary(request.summary());
        entity.setFileUrl(request.fileUrl());

        NewsletterEntity saved = newsletterRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "NEWSLETTER_UPDATED", ENTITY_NEWSLETTER,
                saved.getId(), null);
        return toResponse(saved);
    }

    public void delete(UUID id, ShieldPrincipal principal) {
        NewsletterEntity entity = findNewsletter(id);
        entity.setDeleted(true);
        newsletterRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "NEWSLETTER_DELETED", ENTITY_NEWSLETTER, id, null);
    }

    public NewsletterResponse publish(UUID id, ShieldPrincipal principal) {
        NewsletterEntity entity = findNewsletter(id);
        if (entity.getStatus() != NewsletterStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT newsletters can be published");
        }
        entity.setStatus(NewsletterStatus.PUBLISHED);
        entity.setPublishedAt(Instant.now());
        entity.setPublishedBy(principal.userId());

        NewsletterEntity saved = newsletterRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "NEWSLETTER_PUBLISHED", ENTITY_NEWSLETTER,
                saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public NewsletterResponse getDownloadInfo(UUID id) {
        return toResponse(findNewsletter(id));
    }

    private NewsletterEntity findNewsletter(UUID id) {
        return newsletterRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Newsletter not found: " + id));
    }

    private NewsletterResponse toResponse(NewsletterEntity entity) {
        return new NewsletterResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getSummary(),
                entity.getFileUrl(),
                entity.getYear(),
                entity.getMonth(),
                entity.getStatus(),
                entity.getPublishedAt(),
                entity.getPublishedBy(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
