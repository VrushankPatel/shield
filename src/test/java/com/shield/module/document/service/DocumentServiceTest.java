package com.shield.module.document.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.module.document.dto.DocumentCreateRequest;
import com.shield.module.document.dto.DocumentResponse;
import com.shield.module.document.entity.DocumentEntity;
import com.shield.module.document.repository.DocumentCategoryRepository;
import com.shield.module.document.repository.DocumentRepository;
import com.shield.security.model.ShieldPrincipal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentCategoryRepository documentCategoryRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private AuditLogService auditLogService;

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService(documentCategoryRepository, documentRepository, auditLogService);
    }

    @Test
    void createDocumentShouldSetUploadedByFromPrincipal() {
        when(documentRepository.save(any(DocumentEntity.class))).thenAnswer(invocation -> {
            DocumentEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        DocumentCreateRequest request = new DocumentCreateRequest(
                "Bylaws",
                null,
                "PDF",
                "https://files.example/bylaws.pdf",
                1024L,
                "Society bylaws",
                "v1",
                true,
                null,
                "policy");

        DocumentResponse response = documentService.createDocument(request, principal);

        assertEquals(principal.userId(), response.uploadedBy());
        assertEquals(principal.tenantId(), response.tenantId());
        assertEquals("Bylaws", response.documentName());
    }
}
