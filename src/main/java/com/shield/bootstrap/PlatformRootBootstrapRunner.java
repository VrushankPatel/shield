package com.shield.bootstrap;

import com.shield.module.platform.service.PlatformRootService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlatformRootBootstrapRunner implements CommandLineRunner {

    private final PlatformRootService platformRootService;

    @Override
    public void run(String... args) {
        platformRootService.ensureRootAccountAndGeneratePasswordIfMissing().ifPresent(generatedSecret ->
                log.warn("Platform root credential generated. loginId='root', temporaryCredential='{}'. Change it immediately via /api/v1/platform/root/change-password", generatedSecret));
    }
}
