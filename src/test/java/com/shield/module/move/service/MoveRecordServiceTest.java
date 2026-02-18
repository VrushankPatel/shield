package com.shield.module.move.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.UnauthorizedException;
import com.shield.module.move.dto.MoveRecordCreateRequest;
import com.shield.module.move.repository.MoveRecordRepository;
import com.shield.module.unit.repository.UnitRepository;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.LocalDate;
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
}
