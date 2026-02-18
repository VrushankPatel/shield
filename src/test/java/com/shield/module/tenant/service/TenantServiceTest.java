package com.shield.module.tenant.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.tenant.dto.TenantCreateRequest;
import com.shield.module.tenant.dto.TenantResponse;
import com.shield.module.tenant.dto.TenantUpdateRequest;
import com.shield.module.tenant.entity.TenantEntity;
import com.shield.module.tenant.mapper.TenantMapper;
import com.shield.module.tenant.repository.TenantRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantMapper tenantMapper;

    @Mock
    private AuditLogService auditLogService;

    private TenantService tenantService;

    @BeforeEach
    void setUp() {
        tenantService = new TenantService(tenantRepository, tenantMapper, auditLogService);
    }

    @Test
    void createShouldPersistTenant() {
        UUID tenantId = UUID.randomUUID();

        when(tenantRepository.save(any(TenantEntity.class))).thenAnswer(invocation -> {
            TenantEntity entity = invocation.getArgument(0);
            entity.setId(tenantId);
            return entity;
        });
        when(tenantMapper.toResponse(any(TenantEntity.class))).thenReturn(new TenantResponse(
                tenantId,
                "Test Society",
                "Address",
                Instant.now(),
                Instant.now()));

        TenantResponse response = tenantService.create(new TenantCreateRequest("Test Society", "Address"));

        assertEquals(tenantId, response.id());
        assertEquals("Test Society", response.name());
    }

    @Test
    void getByIdShouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(tenantRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tenantService.getById(id));
    }

    @Test
    void updateShouldMapResponse() {
        UUID id = UUID.randomUUID();

        TenantEntity existing = new TenantEntity();
        existing.setId(id);
        existing.setName("Old");

        when(tenantRepository.findById(id)).thenReturn(Optional.of(existing));
        when(tenantRepository.save(any(TenantEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tenantMapper.toResponse(any(TenantEntity.class))).thenReturn(new TenantResponse(
                id,
                "New",
                "New Address",
                Instant.now(),
                Instant.now()));

        TenantResponse response = tenantService.update(id, new TenantUpdateRequest("New", "New Address"));

        assertEquals("New", response.name());
    }
}
