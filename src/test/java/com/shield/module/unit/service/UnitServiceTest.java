package com.shield.module.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.unit.dto.UnitCreateRequest;
import com.shield.module.unit.dto.UnitResponse;
import com.shield.module.unit.dto.UnitUpdateRequest;
import com.shield.module.unit.entity.UnitEntity;
import com.shield.module.unit.entity.UnitStatus;
import com.shield.module.unit.mapper.UnitMapper;
import com.shield.module.unit.repository.UnitRepository;
import com.shield.tenant.context.TenantContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UnitServiceTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private UnitMapper unitMapper;

    @Mock
    private AuditLogService auditLogService;

    private UnitService unitService;

    @BeforeEach
    void setUp() {
        unitService = new UnitService(unitRepository, unitMapper, auditLogService);
    }

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void createShouldPersistWithTenantContext() {
        UUID tenantId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        when(unitRepository.save(any(UnitEntity.class))).thenAnswer(invocation -> {
            UnitEntity unit = invocation.getArgument(0);
            unit.setId(unitId);
            return unit;
        });

        when(unitMapper.toResponse(any(UnitEntity.class))).thenReturn(new UnitResponse(
                unitId,
                tenantId,
                "A-101",
                "A",
                "FLAT",
                BigDecimal.valueOf(900),
                UnitStatus.ACTIVE,
                Instant.now(),
                Instant.now()));

        UnitResponse response = unitService.create(new UnitCreateRequest(
                "A-101",
                "A",
                "FLAT",
                BigDecimal.valueOf(900),
                UnitStatus.ACTIVE));

        assertEquals(tenantId, response.tenantId());
        assertEquals("A-101", response.unitNumber());
        verify(auditLogService).record(eq(tenantId), eq(null), eq("UNIT_CREATED"), eq("unit"), eq(unitId), any());
    }

    @Test
    void updateShouldThrowWhenUnitMissing() {
        UUID unitId = UUID.randomUUID();
        when(unitRepository.findByIdAndDeletedFalse(unitId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> unitService.update(unitId, new UnitUpdateRequest(
                "A-102",
                "A",
                "FLAT",
                BigDecimal.valueOf(1000),
                UnitStatus.OCCUPIED)));
    }

    @Test
    void listShouldReturnMappedPage() {
        UnitEntity unit = new UnitEntity();
        unit.setId(UUID.randomUUID());
        unit.setTenantId(UUID.randomUUID());
        unit.setUnitNumber("B-201");

        when(unitRepository.findAllByDeletedFalse(Pageable.ofSize(10))).thenReturn(new PageImpl<>(List.of(unit)));
        when(unitMapper.toResponse(unit)).thenReturn(new UnitResponse(
                unit.getId(),
                unit.getTenantId(),
                unit.getUnitNumber(),
                "B",
                "FLAT",
                BigDecimal.valueOf(1100),
                UnitStatus.OCCUPIED,
                Instant.now(),
                Instant.now()));

        assertEquals(1, unitService.list(Pageable.ofSize(10)).content().size());
    }

    @Test
    void getByIdShouldReturnMappedResponse() {
        UUID unitId = UUID.randomUUID();
        UnitEntity unit = new UnitEntity();
        unit.setId(unitId);
        unit.setTenantId(UUID.randomUUID());
        unit.setUnitNumber("C-301");

        when(unitRepository.findByIdAndDeletedFalse(unitId)).thenReturn(Optional.of(unit));
        when(unitMapper.toResponse(unit)).thenReturn(new UnitResponse(
                unitId,
                unit.getTenantId(),
                "C-301",
                "C",
                "FLAT",
                BigDecimal.valueOf(1250),
                UnitStatus.ACTIVE,
                Instant.now(),
                Instant.now()));

        UnitResponse response = unitService.getById(unitId);
        assertEquals("C-301", response.unitNumber());
    }

    @Test
    void updateShouldPersistAndAudit() {
        UUID unitId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        UnitEntity unit = new UnitEntity();
        unit.setId(unitId);
        unit.setTenantId(tenantId);
        unit.setUnitNumber("A-101");
        unit.setStatus(UnitStatus.ACTIVE);

        when(unitRepository.findByIdAndDeletedFalse(unitId)).thenReturn(Optional.of(unit));
        when(unitRepository.save(any(UnitEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitMapper.toResponse(any(UnitEntity.class))).thenAnswer(invocation -> {
            UnitEntity entity = invocation.getArgument(0);
            return new UnitResponse(
                    entity.getId(),
                    entity.getTenantId(),
                    entity.getUnitNumber(),
                    entity.getBlock(),
                    entity.getType(),
                    entity.getSquareFeet(),
                    entity.getStatus(),
                    Instant.now(),
                    Instant.now());
        });

        UnitResponse response = unitService.update(unitId, new UnitUpdateRequest(
                "A-102",
                "A",
                "VILLA",
                BigDecimal.valueOf(1500),
                UnitStatus.OCCUPIED));

        assertEquals("A-102", response.unitNumber());
        assertEquals(UnitStatus.OCCUPIED, response.status());
        verify(auditLogService).record(eq(tenantId), eq(null), eq("UNIT_UPDATED"), eq("unit"), eq(unitId), any());
    }

    @Test
    void deleteShouldSoftDeleteEntity() {
        UUID unitId = UUID.randomUUID();

        UnitEntity unit = new UnitEntity();
        unit.setId(unitId);
        unit.setTenantId(UUID.randomUUID());
        unit.setDeleted(false);

        when(unitRepository.findByIdAndDeletedFalse(unitId)).thenReturn(Optional.of(unit));
        when(unitRepository.save(any(UnitEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        unitService.delete(unitId);

        assertTrue(unit.isDeleted());
    }
}
