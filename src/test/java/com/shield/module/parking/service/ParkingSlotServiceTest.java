package com.shield.module.parking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.module.parking.dto.ParkingSlotAllocateRequest;
import com.shield.module.parking.dto.ParkingSlotCreateRequest;
import com.shield.module.parking.dto.ParkingSlotResponse;
import com.shield.module.parking.entity.ParkingSlotEntity;
import com.shield.module.parking.entity.ParkingType;
import com.shield.module.parking.entity.VehicleType;
import com.shield.module.parking.repository.ParkingSlotRepository;
import com.shield.module.unit.entity.UnitEntity;
import com.shield.module.unit.repository.UnitRepository;
import com.shield.security.model.ShieldPrincipal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParkingSlotServiceTest {

    @Mock
    private ParkingSlotRepository parkingSlotRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private AuditLogService auditLogService;

    private ParkingSlotService parkingSlotService;

    @BeforeEach
    void setUp() {
        parkingSlotService = new ParkingSlotService(parkingSlotRepository, unitRepository, auditLogService);
    }

    @Test
    void createShouldAllocateWhenUnitProvided() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();

        UnitEntity unit = new UnitEntity();
        unit.setId(unitId);
        unit.setTenantId(tenantId);

        when(parkingSlotRepository.existsBySlotNumberIgnoreCaseAndDeletedFalse("A-01")).thenReturn(false);
        when(unitRepository.findByIdAndDeletedFalse(unitId)).thenReturn(Optional.of(unit));
        when(parkingSlotRepository.save(any(ParkingSlotEntity.class))).thenAnswer(invocation -> {
            ParkingSlotEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ShieldPrincipal principal = new ShieldPrincipal(userId, tenantId, "admin@shield.dev", "ADMIN");
        ParkingSlotResponse response = parkingSlotService.create(
                new ParkingSlotCreateRequest("A-01", ParkingType.COVERED, VehicleType.FOUR_WHEELER, unitId),
                principal);

        assertEquals("A-01", response.slotNumber());
        assertEquals(unitId, response.unitId());
        assertEquals(true, response.allocated());
        verify(auditLogService).logEvent(eq(tenantId), eq(userId), eq("PARKING_SLOT_CREATED"), eq("parking_slot"), any(), any());
    }

    @Test
    void allocateShouldFailWhenSlotAlreadyAllocatedToAnotherUnit() {
        UUID slotId = UUID.randomUUID();
        UUID unitA = UUID.randomUUID();
        UUID unitB = UUID.randomUUID();

        ParkingSlotEntity slot = new ParkingSlotEntity();
        slot.setId(slotId);
        slot.setTenantId(UUID.randomUUID());
        slot.setAllocated(true);
        slot.setUnitId(unitA);

        UnitEntity unit = new UnitEntity();
        unit.setId(unitB);

        when(parkingSlotRepository.findByIdAndDeletedFalse(slotId)).thenReturn(Optional.of(slot));
        when(unitRepository.findByIdAndDeletedFalse(unitB)).thenReturn(Optional.of(unit));

        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), slot.getTenantId(), "admin@shield.dev", "ADMIN");

        assertThrows(BadRequestException.class, () -> parkingSlotService.allocate(slotId, new ParkingSlotAllocateRequest(unitB), principal));
    }

    @Test
    void deallocateShouldReturnSameWhenAlreadyFree() {
        UUID slotId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        ParkingSlotEntity slot = new ParkingSlotEntity();
        slot.setId(slotId);
        slot.setTenantId(tenantId);
        slot.setAllocated(false);
        slot.setSlotNumber("A-01");
        slot.setParkingType(ParkingType.COVERED);
        slot.setVehicleType(VehicleType.FOUR_WHEELER);

        when(parkingSlotRepository.findByIdAndDeletedFalse(slotId)).thenReturn(Optional.of(slot));

        ParkingSlotResponse response = parkingSlotService.deallocate(
                slotId,
                new ShieldPrincipal(UUID.randomUUID(), tenantId, "admin@shield.dev", "ADMIN"));

        assertEquals(false, response.allocated());
    }

    @Test
    void deleteShouldRejectAllocatedSlot() {
        UUID slotId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        ParkingSlotEntity slot = new ParkingSlotEntity();
        slot.setId(slotId);
        slot.setTenantId(tenantId);
        slot.setAllocated(true);

        when(parkingSlotRepository.findByIdAndDeletedFalse(slotId)).thenReturn(Optional.of(slot));

        assertThrows(BadRequestException.class, () -> parkingSlotService.delete(
                slotId,
                new ShieldPrincipal(UUID.randomUUID(), tenantId, "admin@shield.dev", "ADMIN")));
    }
}
