package com.shield.bootstrap;

import com.shield.module.platform.service.PlatformRootService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.util.EnumSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlatformRootBootstrapRunner implements CommandLineRunner {

    private final PlatformRootService platformRootService;

    @Value("${shield.platform.root.bootstrap.credential-file:./root-bootstrap-credential.txt}")
    private String bootstrapCredentialFile;

    @Override
    public void run(String... args) {
        platformRootService.ensureRootAccountAndGeneratePasswordIfMissing().ifPresent(this::persistGeneratedCredential);
    }

    private void persistGeneratedCredential(String generatedCredential) {
        Path credentialPath = Paths.get(bootstrapCredentialFile).toAbsolutePath().normalize();
        String content = "loginId=root\n"
                + "credential=" + generatedCredential + "\n"
                + "generatedAt=" + Instant.now() + "\n"
                + "note=Change immediately via /api/v1/platform/root/change-password\n";
        try {
            Path parent = credentialPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(
                    credentialPath,
                    content,
                    StandardCharsets.UTF_8);
            setSecurePermissionsIfSupported(credentialPath);
            log.warn("Platform root credential generated and stored at '{}'. Delete this file after first login.", credentialPath);
        } catch (IOException ex) {
            log.error("Platform root credential was generated but could not be written to '{}'.", credentialPath, ex);
        }
    }

    private void setSecurePermissionsIfSupported(Path credentialPath) {
        try {
            Files.setPosixFilePermissions(
                    credentialPath,
                    EnumSet.of(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE));
        } catch (Exception ignored) {
            // Non-POSIX file systems are acceptable; best-effort only.
        }
    }
}
