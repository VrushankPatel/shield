package com.shield.module.file.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.file.dto.GeneratePresignedUrlRequest;
import com.shield.module.file.entity.StoredFileEntity;
import com.shield.module.file.entity.StoredFileStatus;
import com.shield.module.file.repository.StoredFileRepository;
import com.shield.security.model.ShieldPrincipal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private StoredFileRepository storedFileRepository;

    @Mock
    private AuditLogService auditLogService;

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(storedFileRepository, auditLogService, tempDir.toString());
        fileStorageService.initializeStorage();
    }

    @Test
    void uploadShouldPersistAndReturnMetadata() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID storedId = UUID.randomUUID();
        ShieldPrincipal principal = new ShieldPrincipal(userId, tenantId, "file@shield.dev", "ADMIN");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "hello shield".getBytes());

        when(storedFileRepository.findByFileIdAndDeletedFalse("file-001")).thenReturn(Optional.empty());
        when(storedFileRepository.save(any(StoredFileEntity.class))).thenAnswer(invocation -> {
            StoredFileEntity entity = invocation.getArgument(0);
            entity.setId(storedId);
            entity.setCreatedAt(Instant.now());
            return entity;
        });

        var response = fileStorageService.upload(file, null, "file-001", principal);

        assertEquals(storedId, response.id());
        assertEquals("file-001", response.fileId());
        assertEquals("notes.txt", response.fileName());
        assertEquals(12, response.fileSize());
        verify(auditLogService).logEvent(tenantId, userId, "FILE_UPLOADED", "stored_file", storedId, null);
    }

    @Test
    void uploadShouldRejectDuplicateFileId() {
        StoredFileEntity existing = new StoredFileEntity();
        existing.setFileId("dup-file");

        when(storedFileRepository.findByFileIdAndDeletedFalse("dup-file")).thenReturn(Optional.of(existing));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "x.txt",
                "text/plain",
                "abc".getBytes());
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "x@shield.dev", "ADMIN");

        assertThrows(BadRequestException.class, () -> fileStorageService.upload(
                file,
                null,
                "dup-file",
                principal));
    }

    @Test
    void downloadShouldThrowForExpiredFile() {
        StoredFileEntity entity = new StoredFileEntity();
        entity.setId(UUID.randomUUID());
        entity.setFileId("exp-file");
        entity.setStatus(StoredFileStatus.ACTIVE);
        entity.setStoragePath(tempDir.resolve("missing.txt").toString());
        entity.setExpiresAt(Instant.now().minusSeconds(5));

        when(storedFileRepository.findByFileIdAndDeletedFalse("exp-file")).thenReturn(Optional.of(entity));
        when(storedFileRepository.save(any(StoredFileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "x@shield.dev", "ADMIN");

        assertThrows(ResourceNotFoundException.class, () -> fileStorageService.download(
                "exp-file",
                principal));

        assertEquals(StoredFileStatus.EXPIRED, entity.getStatus());
    }

    @Test
    void deleteShouldSoftDeleteAndRemovePhysicalFile() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Path filePath = tempDir.resolve("to-delete.txt");
        Files.writeString(filePath, "cleanup");

        StoredFileEntity entity = new StoredFileEntity();
        entity.setId(UUID.randomUUID());
        entity.setFileId("del-file");
        entity.setStatus(StoredFileStatus.ACTIVE);
        entity.setStoragePath(filePath.toString());

        when(storedFileRepository.findByFileIdAndDeletedFalse("del-file")).thenReturn(Optional.of(entity));
        when(storedFileRepository.save(any(StoredFileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        fileStorageService.delete("del-file", new ShieldPrincipal(userId, tenantId, "x@shield.dev", "ADMIN"));

        assertTrue(entity.isDeleted());
        assertEquals(StoredFileStatus.DELETED, entity.getStatus());
        assertFalse(Files.exists(filePath));
    }

    @Test
    void generatePresignedUrlShouldReturnEncodedUploadUrl() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var response = fileStorageService.generatePresignedUrl(
                new GeneratePresignedUrlRequest("Gate pass image.png", "image/png", 10),
                new ShieldPrincipal(userId, tenantId, "x@shield.dev", "ADMIN"));

        assertNotNull(response.fileId());
        assertTrue(response.uploadUrl().contains("fileName=Gate+pass+image.png"));
        assertNotNull(response.expiresAt());
    }

    @Test
    void downloadShouldReturnContentWhenFileExists() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Path filePath = tempDir.resolve("download.txt");
        byte[] payload = "binary-content".getBytes();
        Files.write(filePath, payload);

        StoredFileEntity entity = new StoredFileEntity();
        entity.setId(UUID.randomUUID());
        entity.setFileId("ok-file");
        entity.setFileName("download.txt");
        entity.setContentType("text/plain");
        entity.setStatus(StoredFileStatus.ACTIVE);
        entity.setStoragePath(filePath.toString());

        when(storedFileRepository.findByFileIdAndDeletedFalse("ok-file")).thenReturn(Optional.of(entity));

        var response = fileStorageService.download("ok-file", new ShieldPrincipal(userId, tenantId, "x@shield.dev", "ADMIN"));

        assertEquals("download.txt", response.fileName());
        assertEquals("text/plain", response.contentType());
        assertArrayEquals(payload, response.content());
        verify(auditLogService).logEvent(tenantId, userId, "FILE_DOWNLOADED", "stored_file", entity.getId(), null);
    }
}
