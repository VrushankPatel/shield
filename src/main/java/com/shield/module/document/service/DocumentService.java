package com.shield.module.document.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.document.dto.DocumentAccessLogResponse;
import com.shield.module.document.dto.DocumentCategoryCreateRequest;
import com.shield.module.document.dto.DocumentCategoryResponse;
import com.shield.module.document.dto.DocumentCategoryUpdateRequest;
import com.shield.module.document.dto.DocumentCreateRequest;
import com.shield.module.document.dto.DocumentDownloadResponse;
import com.shield.module.document.dto.DocumentResponse;
import com.shield.module.document.dto.DocumentUpdateRequest;
import com.shield.module.document.entity.DocumentAccessLogEntity;
import com.shield.module.document.entity.DocumentAccessType;
import com.shield.module.document.entity.DocumentCategoryEntity;
import com.shield.module.document.entity.DocumentEntity;
import com.shield.module.document.repository.DocumentAccessLogRepository;
import com.shield.module.document.repository.DocumentCategoryRepository;
import com.shield.module.document.repository.DocumentRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DocumentService {

    private static final String ENTITY_DOCUMENT_CATEGORY = "document_category";
    private static final String ENTITY_DOCUMENT = "document";
    private static final String DOCUMENT_CATEGORY_NOT_FOUND_PREFIX = "Document category not found: ";
    private static final String DOCUMENT_NOT_FOUND_PREFIX = "Document not found: ";

    private final DocumentCategoryRepository documentCategoryRepository;
    private final DocumentRepository documentRepository;
    private final DocumentAccessLogRepository documentAccessLogRepository;
    private final AuditLogService auditLogService;

    public DocumentService(
            DocumentCategoryRepository documentCategoryRepository,
            DocumentRepository documentRepository,
            DocumentAccessLogRepository documentAccessLogRepository,
            AuditLogService auditLogService) {
        this.documentCategoryRepository = documentCategoryRepository;
        this.documentRepository = documentRepository;
        this.documentAccessLogRepository = documentAccessLogRepository;
        this.auditLogService = auditLogService;
    }

    public DocumentCategoryResponse createCategory(DocumentCategoryCreateRequest request, ShieldPrincipal principal) {
        DocumentCategoryEntity entity = new DocumentCategoryEntity();
        entity.setTenantId(principal.tenantId());
        entity.setCategoryName(request.categoryName());
        entity.setDescription(request.description());
        entity.setParentCategoryId(request.parentCategoryId());

        DocumentCategoryEntity saved = documentCategoryRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DOCUMENT_CATEGORY_CREATED", ENTITY_DOCUMENT_CATEGORY, saved.getId(), null);
        return toCategoryResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DocumentCategoryResponse> listCategories(Pageable pageable) {
        return PagedResponse.from(documentCategoryRepository.findAllByDeletedFalse(pageable).map(this::toCategoryResponse));
    }

    @Transactional(readOnly = true)
    public DocumentCategoryResponse getCategory(UUID id) {
        DocumentCategoryEntity entity = documentCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(DOCUMENT_CATEGORY_NOT_FOUND_PREFIX + id));
        return toCategoryResponse(entity);
    }

    public DocumentCategoryResponse updateCategory(UUID id, DocumentCategoryUpdateRequest request, ShieldPrincipal principal) {
        DocumentCategoryEntity entity = documentCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(DOCUMENT_CATEGORY_NOT_FOUND_PREFIX + id));

        entity.setCategoryName(request.categoryName());
        entity.setDescription(request.description());
        entity.setParentCategoryId(request.parentCategoryId());

        DocumentCategoryEntity saved = documentCategoryRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DOCUMENT_CATEGORY_UPDATED", ENTITY_DOCUMENT_CATEGORY, saved.getId(), null);
        return toCategoryResponse(saved);
    }

    public void deleteCategory(UUID id, ShieldPrincipal principal) {
        DocumentCategoryEntity entity = documentCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(DOCUMENT_CATEGORY_NOT_FOUND_PREFIX + id));

        entity.setDeleted(true);
        documentCategoryRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DOCUMENT_CATEGORY_DELETED", ENTITY_DOCUMENT_CATEGORY, entity.getId(), null);
    }

    public DocumentResponse createDocument(DocumentCreateRequest request, ShieldPrincipal principal) {
        DocumentEntity entity = new DocumentEntity();
        entity.setTenantId(principal.tenantId());
        entity.setDocumentName(request.documentName());
        entity.setCategoryId(request.categoryId());
        entity.setDocumentType(request.documentType());
        entity.setFileUrl(request.fileUrl());
        entity.setFileSize(request.fileSize());
        entity.setDescription(request.description());
        entity.setVersionLabel(request.versionLabel());
        entity.setPublicAccess(request.publicAccess());
        entity.setUploadedBy(principal.userId());
        entity.setUploadDate(Instant.now());
        entity.setExpiryDate(request.expiryDate());
        entity.setTags(request.tags());

        DocumentEntity saved = documentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DOCUMENT_CREATED", ENTITY_DOCUMENT, saved.getId(), null);
        return toDocumentResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DocumentResponse> listDocuments(Pageable pageable) {
        return PagedResponse.from(documentRepository.findAllByDeletedFalse(pageable).map(this::toDocumentResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<DocumentResponse> listPublicDocuments(Pageable pageable) {
        return PagedResponse.from(documentRepository.findAllByPublicAccessTrueAndDeletedFalse(pageable).map(this::toDocumentResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<DocumentResponse> listDocumentsByCategory(UUID categoryId, Pageable pageable) {
        return PagedResponse.from(documentRepository.findAllByCategoryIdAndDeletedFalse(categoryId, pageable).map(this::toDocumentResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<DocumentResponse> searchDocuments(String query, Pageable pageable) {
        return PagedResponse.from(documentRepository.searchByQuery(query, pageable).map(this::toDocumentResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<DocumentResponse> listDocumentsByTag(String tag, Pageable pageable) {
        return PagedResponse.from(documentRepository.findAllByTag(tag, pageable).map(this::toDocumentResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<DocumentResponse> listExpiringDocuments(LocalDate from, LocalDate to, Pageable pageable) {
        LocalDate fromDate = from != null ? from : LocalDate.now();
        LocalDate toDate = to != null ? to : fromDate.plusDays(30);
        if (toDate.isBefore(fromDate)) {
            throw new BadRequestException("to date cannot be before from date");
        }
        return PagedResponse.from(documentRepository.findAllByExpiryDateBetweenAndDeletedFalse(fromDate, toDate, pageable).map(this::toDocumentResponse));
    }

    @Transactional
    public DocumentResponse getDocument(UUID id, ShieldPrincipal principal) {
        DocumentEntity entity = documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(DOCUMENT_NOT_FOUND_PREFIX + id));
        recordAccess(entity, principal, DocumentAccessType.VIEW);
        return toDocumentResponse(entity);
    }

    public DocumentDownloadResponse downloadDocument(UUID id, ShieldPrincipal principal) {
        DocumentEntity entity = documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(DOCUMENT_NOT_FOUND_PREFIX + id));
        Instant downloadedAt = Instant.now();
        recordAccess(entity, principal, DocumentAccessType.DOWNLOAD, downloadedAt);
        return new DocumentDownloadResponse(entity.getId(), entity.getDocumentName(), entity.getFileUrl(), downloadedAt);
    }

    public DocumentResponse updateDocument(UUID id, DocumentUpdateRequest request, ShieldPrincipal principal) {
        DocumentEntity entity = documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(DOCUMENT_NOT_FOUND_PREFIX + id));

        entity.setDocumentName(request.documentName());
        entity.setCategoryId(request.categoryId());
        entity.setDocumentType(request.documentType());
        entity.setFileUrl(request.fileUrl());
        entity.setFileSize(request.fileSize());
        entity.setDescription(request.description());
        entity.setVersionLabel(request.versionLabel());
        entity.setPublicAccess(request.publicAccess());
        entity.setExpiryDate(request.expiryDate());
        entity.setTags(request.tags());

        DocumentEntity saved = documentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DOCUMENT_UPDATED", ENTITY_DOCUMENT, saved.getId(), null);
        return toDocumentResponse(saved);
    }

    public void deleteDocument(UUID id, ShieldPrincipal principal) {
        DocumentEntity entity = documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(DOCUMENT_NOT_FOUND_PREFIX + id));

        entity.setDeleted(true);
        documentRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DOCUMENT_DELETED", ENTITY_DOCUMENT, entity.getId(), null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DocumentAccessLogResponse> listAccessLogsByDocument(UUID documentId, Pageable pageable) {
        return PagedResponse.from(documentAccessLogRepository.findAllByDocumentIdAndDeletedFalse(documentId, pageable)
                .map(this::toDocumentAccessLogResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<DocumentAccessLogResponse> listAccessLogsByUser(UUID userId, Pageable pageable) {
        return PagedResponse.from(documentAccessLogRepository.findAllByAccessedByAndDeletedFalse(userId, pageable)
                .map(this::toDocumentAccessLogResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<DocumentAccessLogResponse> listAccessLogsByDateRange(Instant from, Instant to, Pageable pageable) {
        if (from == null || to == null) {
            throw new BadRequestException("Both from and to date-time values are required");
        }
        if (to.isBefore(from)) {
            throw new BadRequestException("to date-time cannot be before from date-time");
        }
        return PagedResponse.from(documentAccessLogRepository.findAllByAccessedAtBetweenAndDeletedFalse(from, to, pageable)
                .map(this::toDocumentAccessLogResponse));
    }

    private void recordAccess(DocumentEntity document, ShieldPrincipal principal, DocumentAccessType accessType) {
        recordAccess(document, principal, accessType, Instant.now());
    }

    private void recordAccess(DocumentEntity document, ShieldPrincipal principal, DocumentAccessType accessType, Instant accessedAt) {
        DocumentAccessLogEntity entity = new DocumentAccessLogEntity();
        entity.setTenantId(document.getTenantId());
        entity.setDocumentId(document.getId());
        entity.setAccessedBy(principal.userId());
        entity.setAccessType(accessType);
        entity.setAccessedAt(accessedAt);
        documentAccessLogRepository.save(entity);
    }

    private DocumentCategoryResponse toCategoryResponse(DocumentCategoryEntity entity) {
        return new DocumentCategoryResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getCategoryName(),
                entity.getDescription(),
                entity.getParentCategoryId());
    }

    private DocumentResponse toDocumentResponse(DocumentEntity entity) {
        return new DocumentResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getDocumentName(),
                entity.getCategoryId(),
                entity.getDocumentType(),
                entity.getFileUrl(),
                entity.getFileSize(),
                entity.getDescription(),
                entity.getVersionLabel(),
                entity.isPublicAccess(),
                entity.getUploadedBy(),
                entity.getUploadDate(),
                entity.getExpiryDate(),
                entity.getTags());
    }

    private DocumentAccessLogResponse toDocumentAccessLogResponse(DocumentAccessLogEntity entity) {
        return new DocumentAccessLogResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getDocumentId(),
                entity.getAccessedBy(),
                entity.getAccessType(),
                entity.getAccessedAt());
    }
}
