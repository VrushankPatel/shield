package com.shield.module.document.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.document.dto.DocumentCategoryCreateRequest;
import com.shield.module.document.dto.DocumentCategoryResponse;
import com.shield.module.document.dto.DocumentCategoryUpdateRequest;
import com.shield.module.document.dto.DocumentCreateRequest;
import com.shield.module.document.dto.DocumentResponse;
import com.shield.module.document.dto.DocumentUpdateRequest;
import com.shield.module.document.entity.DocumentCategoryEntity;
import com.shield.module.document.entity.DocumentEntity;
import com.shield.module.document.repository.DocumentCategoryRepository;
import com.shield.module.document.repository.DocumentRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DocumentService {

    private final DocumentCategoryRepository documentCategoryRepository;
    private final DocumentRepository documentRepository;
    private final AuditLogService auditLogService;

    public DocumentService(
            DocumentCategoryRepository documentCategoryRepository,
            DocumentRepository documentRepository,
            AuditLogService auditLogService) {
        this.documentCategoryRepository = documentCategoryRepository;
        this.documentRepository = documentRepository;
        this.auditLogService = auditLogService;
    }

    public DocumentCategoryResponse createCategory(DocumentCategoryCreateRequest request, ShieldPrincipal principal) {
        DocumentCategoryEntity entity = new DocumentCategoryEntity();
        entity.setTenantId(principal.tenantId());
        entity.setCategoryName(request.categoryName());
        entity.setDescription(request.description());
        entity.setParentCategoryId(request.parentCategoryId());

        DocumentCategoryEntity saved = documentCategoryRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "DOCUMENT_CATEGORY_CREATED", "document_category", saved.getId(), null);
        return toCategoryResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DocumentCategoryResponse> listCategories(Pageable pageable) {
        return PagedResponse.from(documentCategoryRepository.findAllByDeletedFalse(pageable).map(this::toCategoryResponse));
    }

    @Transactional(readOnly = true)
    public DocumentCategoryResponse getCategory(UUID id) {
        DocumentCategoryEntity entity = documentCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document category not found: " + id));
        return toCategoryResponse(entity);
    }

    public DocumentCategoryResponse updateCategory(UUID id, DocumentCategoryUpdateRequest request, ShieldPrincipal principal) {
        DocumentCategoryEntity entity = documentCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document category not found: " + id));

        entity.setCategoryName(request.categoryName());
        entity.setDescription(request.description());
        entity.setParentCategoryId(request.parentCategoryId());

        DocumentCategoryEntity saved = documentCategoryRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "DOCUMENT_CATEGORY_UPDATED", "document_category", saved.getId(), null);
        return toCategoryResponse(saved);
    }

    public void deleteCategory(UUID id, ShieldPrincipal principal) {
        DocumentCategoryEntity entity = documentCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document category not found: " + id));

        entity.setDeleted(true);
        documentCategoryRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "DOCUMENT_CATEGORY_DELETED", "document_category", entity.getId(), null);
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
        auditLogService.record(principal.tenantId(), principal.userId(), "DOCUMENT_CREATED", "document", saved.getId(), null);
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
    public DocumentResponse getDocument(UUID id) {
        DocumentEntity entity = documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
        return toDocumentResponse(entity);
    }

    public DocumentResponse updateDocument(UUID id, DocumentUpdateRequest request, ShieldPrincipal principal) {
        DocumentEntity entity = documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

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
        auditLogService.record(principal.tenantId(), principal.userId(), "DOCUMENT_UPDATED", "document", saved.getId(), null);
        return toDocumentResponse(saved);
    }

    public void deleteDocument(UUID id, ShieldPrincipal principal) {
        DocumentEntity entity = documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

        entity.setDeleted(true);
        documentRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "DOCUMENT_DELETED", "document", entity.getId(), null);
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
}
