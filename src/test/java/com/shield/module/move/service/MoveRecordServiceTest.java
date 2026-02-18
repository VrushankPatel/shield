package com.shield.module.move.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.UnauthorizedException;
import com.shield.module.move.dto.MoveRecordCreateRequest;
import com.shield.module.move.dto.MoveRecordDecisionRequest;
import com.shield.module.move.dto.MoveRecordResponse;
import com.shield.module.move.entity.MoveRecordEntity;
import com.shield.module.move.entity.MoveStatus;
import com.shield.module.move.repository.MoveRecordRepository;
import com.shield.module.unit.entity.UnitEntity;
import com.shield.module.unit.repository.UnitRepository;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.model.ShieldPrincipal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MoveRecordServiceTest {

    @Mock
    private MoveRecordRepository moveRecordRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    private MoveRecordService moveRecordService;

    @BeforeEach
    void setUp() {
        moveRecordService = new MoveRecordService(moveRecordRepository, unitRepository, userRepository, auditLogService);
    }

    @Test
    void createMoveInShouldRejectWhenResidentCreatesForAnotherUser() {
        ShieldPrincipal principal = new ShieldPrincipal(UUID.randomUUID(), UUID.randomUUID(), "tenant@shield.dev", "TENANT");
        MoveRecordCreateRequest request = new MoveRecordCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now().plusDays(1),
                null,
                null);

        assertThrows(UnauthorizedException.class, () -> moveRecordService.createMoveIn(request, principal));
    }

    @Test
    void createMoveInShouldPersistWhenSelfUser() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();

        ShieldPrincipal principal = new ShieldPrincipal(userId, tenantId, "resident@shield.dev", "TENANT");

        UnitEntity unit = new UnitEntity();
        unit.setId(unitId);

        UserEntity user = new UserEntity();
        user.setId(userId);

        when(unitRepository.findByIdAndDeletedFalse(unitId)).thenReturn(Optional.of(unit));
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(moveRecordRepository.save(any(MoveRecordEntity.class))).thenAnswer(invocation -> {
            MoveRecordEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        MoveRecordResponse response = moveRecordService.createMoveIn(
                new MoveRecordCreateRequest(unitId, userId, LocalDate.now().plusDays(2), BigDecimal.valueOf(1000), "url"),
                principal);

        assertEquals(MoveStatus.PENDING, response.status());
        assertEquals(unitId, response.unitId());
        assertEquals(userId, response.userId());
    }

    @Test
    void approveShouldUpdateStatus() {
        UUID tenantId = UUID.randomUUID();
        UUID approverId = UUID.randomUUID();
        UUID moveId = UUID.randomUUID();

        MoveRecordEntity entity = new MoveRecordEntity();
        entity.setId(moveId);
        entity.setTenantId(tenantId);
        entity.setUserId(UUID.randomUUID());
        entity.setStatus(MoveStatus.PENDING);

        when(moveRecordRepository.findByIdAndDeletedFalse(moveId)).thenReturn(Optional.of(entity));
        when(moveRecordRepository.save(any(MoveRecordEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShieldPrincipal principal = new ShieldPrincipal(approverId, tenantId, "admin@shield.dev", "ADMIN");
        MoveRecordResponse response = moveRecordService.approve(moveId, new MoveRecordDecisionRequest("approved"), principal);

        assertEquals(MoveStatus.APPROVED, response.status());
        assertEquals(approverId, response.approvedBy());
    }
}
