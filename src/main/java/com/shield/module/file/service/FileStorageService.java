package com.shield.module.file.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.file.dto.GeneratePresignedUrlRequest;
import com.shield.module.file.dto.GeneratePresignedUrlResponse;
import com.shield.module.file.dto.StoredFileResponse;
import com.shield.module.file.entity.StoredFileEntity;
import com.shield.module.file.entity.StoredFileStatus;
import com.shield.module.file.repository.StoredFileRepository;
import com.shield.security.model.ShieldPrincipal;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class FileStorageService {

    private static final int DEFAULT_PRESIGNED_EXPIRY_MINUTES = 15;
    private static final String ENTITY_STORED_FILE = "stored_file";
    private static final long DEFAULT_MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    private final StoredFileRepository storedFileRepository;
    private final AuditLogService auditLogService;
    private final FileMalwareScanner fileMalwareScanner;
    private final Path storageRootPath;
    private final long maxFileSizeBytes;
    private final Set<String> allowedContentTypes;
    private final boolean malwareScanEnabled;

    public FileStorageService(
            StoredFileRepository storedFileRepository,
            AuditLogService auditLogService,
            FileMalwareScanner fileMalwareScanner,
            @Value("${shield.files.storage-path:./storage/files}") String storagePath,
            @Value("${shield.files.max-size-bytes:10485760}") long maxFileSizeBytes,
            @Value("${shield.files.allowed-content-types:application/pdf,image/jpeg,image/png,text/plain}") String allowedContentTypes,
            @Value("${shield.files.malware-scan-enabled:false}") boolean malwareScanEnabled) {
        this.storedFileRepository = storedFileRepository;
        this.auditLogService = auditLogService;
        this.fileMalwareScanner = fileMalwareScanner;
        this.storageRootPath = Paths.get(storagePath).toAbsolutePath().normalize();
        this.maxFileSizeBytes = maxFileSizeBytes <= 0 ? DEFAULT_MAX_FILE_SIZE_BYTES : maxFileSizeBytes;
        this.allowedContentTypes = parseAllowedContentTypes(allowedContentTypes);
        this.malwareScanEnabled = malwareScanEnabled;
    }

    @PostConstruct
    void initializeStorage() {
        try {
            Files.createDirectories(storageRootPath);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to initialize local file storage", ex);
        }
    }

    public StoredFileResponse upload(
            MultipartFile file,
            Instant expiresAt,
            String requestedFileId,
            ShieldPrincipal principal) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        String fileId = normalizeFileId(requestedFileId);
        String sanitizedName = sanitizeFileName(file.getOriginalFilename());
        String normalizedContentType = normalizeContentType(file.getContentType());
        validateUploadPolicy(file.getSize(), normalizedContentType, sanitizedName);
        if (storedFileRepository.findByFileIdAndDeletedFalse(fileId).isPresent()) {
            throw new BadRequestException("File id already exists: " + fileId);
        }

        Path tenantDirectory = resolveTenantDirectory(principal.tenantId());
        Path targetFile = tenantDirectory.resolve(fileId + "_" + sanitizedName);

        try {
            byte[] content = file.getBytes();
            runMalwareScanIfEnabled(sanitizedName, normalizedContentType, content);
            Files.write(targetFile, content);

            StoredFileEntity entity = new StoredFileEntity();
            entity.setTenantId(principal.tenantId());
            entity.setFileId(fileId);
            entity.setFileName(sanitizedName);
            entity.setContentType(normalizedContentType);
            entity.setFileSize(content.length);
            entity.setStoragePath(targetFile.toString());
            entity.setUploadedBy(principal.userId());
            entity.setChecksum(sha256Hex(content));
            entity.setStatus(StoredFileStatus.ACTIVE);
            entity.setExpiresAt(expiresAt);

            StoredFileEntity saved = storedFileRepository.save(entity);
            auditLogService.logEvent(
                    principal.tenantId(),
                    principal.userId(),
                    "FILE_UPLOADED",
                    ENTITY_STORED_FILE,
                    saved.getId(),
                    null);
            return toResponse(saved);
        } catch (IOException ex) {
            throw new BadRequestException("Failed to persist file");
        }
    }

    public List<StoredFileResponse> uploadMultiple(
            List<MultipartFile> files,
            Instant expiresAt,
            ShieldPrincipal principal) {

        if (files == null || files.isEmpty()) {
            throw new BadRequestException("At least one file is required");
        }

        List<StoredFileResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            responses.add(upload(file, expiresAt, null, principal));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public StoredFileResponse getMetadata(String fileId, ShieldPrincipal principal) {
        StoredFileEntity entity = findActiveFile(principal.tenantId(), fileId);
        return toResponse(entity);
    }

    public FileDownloadPayload download(String fileId, ShieldPrincipal principal) {
        StoredFileEntity entity = findActiveFile(principal.tenantId(), fileId);

        if (entity.getExpiresAt() != null && entity.getExpiresAt().isBefore(Instant.now())) {
            entity.setStatus(StoredFileStatus.EXPIRED);
            storedFileRepository.save(entity);
            throw new ResourceNotFoundException("File has expired: " + fileId);
        }

        Path path = Paths.get(entity.getStoragePath());
        try {
            byte[] content = Files.readAllBytes(path);
            auditLogService.logEvent(
                    principal.tenantId(),
                    principal.userId(),
                    "FILE_DOWNLOADED",
                    ENTITY_STORED_FILE,
                    entity.getId(),
                    null);
            return new FileDownloadPayload(
                    entity.getFileName(),
                    entity.getContentType(),
                    content);
        } catch (IOException ex) {
            throw new ResourceNotFoundException("Stored file not found on disk: " + fileId);
        }
    }

    public void delete(String fileId, ShieldPrincipal principal) {
        StoredFileEntity entity = findActiveFile(principal.tenantId(), fileId);
        entity.setDeleted(true);
        entity.setStatus(StoredFileStatus.DELETED);

        storedFileRepository.save(entity);

        try {
            Files.deleteIfExists(Paths.get(entity.getStoragePath()));
        } catch (IOException ignored) {
            // Metadata is source-of-truth; physical cleanup can be retried asynchronously.
        }

        auditLogService.logEvent(
                principal.tenantId(),
                principal.userId(),
                "FILE_DELETED",
                ENTITY_STORED_FILE,
                entity.getId(),
                null);
    }

    public GeneratePresignedUrlResponse generatePresignedUrl(
            GeneratePresignedUrlRequest request,
            ShieldPrincipal principal) {

        String sanitizedFileName = sanitizeFileName(request.fileName());
        String normalizedContentType = normalizeContentType(request.contentType());
        validateContentType(normalizedContentType);

        int ttlMinutes = request.expiresInMinutes() == null
                ? DEFAULT_PRESIGNED_EXPIRY_MINUTES
                : request.expiresInMinutes();

        String fileId = generateFileId();
        Instant expiresAt = Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES);
        String encodedName = URLEncoder.encode(sanitizedFileName, StandardCharsets.UTF_8);
        String uploadUrl = "/api/v1/files/upload?fileId=" + fileId + "&fileName=" + encodedName;

        auditLogService.logEvent(
                principal.tenantId(),
                principal.userId(),
                "FILE_PRESIGNED_URL_GENERATED",
                ENTITY_STORED_FILE,
                null,
                null);

        return new GeneratePresignedUrlResponse(fileId, uploadUrl, expiresAt);
    }

    private StoredFileEntity findActiveFile(UUID tenantId, String fileId) {
        StoredFileEntity entity = storedFileRepository.findByFileIdAndDeletedFalse(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Stored file not found: " + fileId));

        if (!entity.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Stored file not found: " + fileId);
        }

        if (entity.getStatus() == StoredFileStatus.DELETED) {
            throw new ResourceNotFoundException("Stored file not found: " + fileId);
        }
        return entity;
    }

    private Path resolveTenantDirectory(UUID tenantId) {
        Path tenantDirectory = storageRootPath.resolve(tenantId.toString());
        try {
            Files.createDirectories(tenantDirectory);
        } catch (IOException ex) {
            throw new BadRequestException("Unable to create storage directory");
        }
        return tenantDirectory;
    }

    private String sanitizeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "file.bin";
        }

        String sanitized = Paths.get(originalFileName).getFileName().toString().trim();
        sanitized = sanitized.replaceAll("[\\r\\n\\t]", "_");
        sanitized = sanitized.replaceAll("[^A-Za-z0-9._-]", "_");
        if (sanitized.isBlank()) {
            return "file.bin";
        }
        if (sanitized.length() > 255) {
            return sanitized.substring(sanitized.length() - 255);
        }
        return sanitized;
    }

    private String normalizeFileId(String requestedFileId) {
        if (requestedFileId == null || requestedFileId.isBlank()) {
            return generateFileId();
        }

        String normalized = requestedFileId.trim();
        if (normalized.length() > 120) {
            throw new BadRequestException("fileId must be <= 120 characters");
        }
        if (!normalized.matches("[A-Za-z0-9_-]+")) {
            throw new BadRequestException("fileId contains invalid characters");
        }
        return normalized;
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new BadRequestException("contentType is required");
        }
        return contentType.toLowerCase(Locale.ROOT).trim();
    }

    private void validateUploadPolicy(long fileSize, String contentType, String fileName) {
        validateContentType(contentType);
        if (fileSize <= 0) {
            throw new BadRequestException("File is empty");
        }
        if (fileSize > maxFileSizeBytes) {
            throw new BadRequestException("File size exceeds maximum allowed bytes: " + maxFileSizeBytes);
        }
        if (fileName.isBlank()) {
            throw new BadRequestException("fileName is required");
        }
    }

    private void validateContentType(String contentType) {
        if (!allowedContentTypes.contains(contentType)) {
            throw new BadRequestException("contentType is not allowed: " + contentType);
        }
    }

    private Set<String> parseAllowedContentTypes(String configuredTypes) {
        Set<String> parsed = new LinkedHashSet<>();
        if (configuredTypes != null) {
            String[] split = configuredTypes.split(",");
            for (String value : split) {
                String normalized = value.trim().toLowerCase(Locale.ROOT);
                if (!normalized.isBlank()) {
                    parsed.add(normalized);
                }
            }
        }
        if (parsed.isEmpty()) {
            parsed.add("application/pdf");
            parsed.add("image/jpeg");
            parsed.add("image/png");
            parsed.add("text/plain");
        }
        return parsed;
    }

    private void runMalwareScanIfEnabled(String fileName, String contentType, byte[] content) {
        if (!malwareScanEnabled) {
            return;
        }

        FileScanResult result = fileMalwareScanner.scan(fileName, contentType, content);
        if (!result.safe()) {
            throw new BadRequestException("File rejected by malware policy: " + result.reason());
        }
    }

    private String generateFileId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String sha256Hex(byte[] content) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(messageDigest.digest(content));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private StoredFileResponse toResponse(StoredFileEntity entity) {
        return new StoredFileResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getFileId(),
                entity.getFileName(),
                entity.getContentType(),
                entity.getFileSize(),
                entity.getChecksum(),
                entity.getStatus(),
                entity.getUploadedBy(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public record FileDownloadPayload(
            String fileName,
            String contentType,
            byte[] content
    ) {
    }
}
