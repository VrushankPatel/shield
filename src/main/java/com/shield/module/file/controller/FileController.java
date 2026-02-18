package com.shield.module.file.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.file.dto.GeneratePresignedUrlRequest;
import com.shield.module.file.dto.GeneratePresignedUrlResponse;
import com.shield.module.file.dto.StoredFileResponse;
import com.shield.module.file.service.FileStorageService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<StoredFileResponse>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "expiresAt", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant expiresAt,
            @RequestParam(value = "fileId", required = false) String fileId) {

        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        StoredFileResponse response = fileStorageService.upload(file, expiresAt, fileId, principal);
        return ResponseEntity.ok(ApiResponse.ok("File uploaded", response));
    }

    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<List<StoredFileResponse>>> uploadMultiple(
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam(value = "expiresAt", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant expiresAt) {

        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        List<StoredFileResponse> response = fileStorageService.uploadMultiple(files, expiresAt, principal);
        return ResponseEntity.ok(ApiResponse.ok("Files uploaded", response));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse<StoredFileResponse>> getMetadata(@PathVariable @Size(max = 120) String fileId) {
        return ResponseEntity.ok(ApiResponse.ok("File metadata fetched", fileStorageService.getMetadata(fileId)));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<byte[]> download(@PathVariable @Size(max = 120) String fileId) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        FileStorageService.FileDownloadPayload payload = fileStorageService.download(fileId, principal);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (payload.contentType() != null && !payload.contentType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(payload.contentType());
            } catch (InvalidMediaTypeException ignored) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + payload.fileName() + "\"")
                .body(payload.content());
    }

    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable @Size(max = 120) String fileId) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        fileStorageService.delete(fileId, principal);
        return ResponseEntity.ok(ApiResponse.ok("File deleted", null));
    }

    @PostMapping("/generate-presigned-url")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE','SECURITY')")
    public ResponseEntity<ApiResponse<GeneratePresignedUrlResponse>> generatePresignedUrl(
            @Valid @RequestBody GeneratePresignedUrlRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                "Presigned URL generated",
                fileStorageService.generatePresignedUrl(request, principal)));
    }
}
