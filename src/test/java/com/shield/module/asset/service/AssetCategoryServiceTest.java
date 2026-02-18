package com.shield.module.asset.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.asset.dto.AssetCategoryCreateRequest;
import com.shield.module.asset.dto.AssetCategoryResponse;
import com.shield.module.asset.entity.AssetCategoryEntity;
import com.shield.module.asset.repository.AssetCategoryRepository;
import com.shield.security.model.ShieldPrincipal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AssetCategoryServiceTest {

    @Mock
    private AssetCategoryRepository assetCategoryRepository;

    @Mock
    private AuditLogService auditLogService;

    private AssetCategoryService assetCategoryService;

    @BeforeEach
    void setUp() {
        assetCategoryService = new AssetCategoryService(assetCategoryRepository, auditLogService);
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "admin@shield.dev", "ADMIN");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createShouldPersistCategory() {
        when(assetCategoryRepository.save(any(AssetCategoryEntity.class))).thenAnswer(invocation -> {
            AssetCategoryEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        AssetCategoryResponse response = assetCategoryService.create(new AssetCategoryCreateRequest("Electrical", "Lights and motors"));

        assertEquals("Electrical", response.categoryName());
    }

    @Test
    void getByIdShouldThrowForMissingCategory() {
        UUID id = UUID.randomUUID();
        when(assetCategoryRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> assetCategoryService.getById(id));
    }
}
