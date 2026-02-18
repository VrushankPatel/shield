package com.shield.module.user.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.user.dto.UserBulkImportRequest;
import com.shield.module.user.dto.UserBulkImportResponse;
import com.shield.module.user.dto.UserCreateRequest;
import com.shield.module.user.dto.UserResponse;
import com.shield.module.user.dto.UserUpdateRequest;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.entity.UserRole;
import com.shield.module.user.entity.UserStatus;
import com.shield.module.user.mapper.UserMapper;
import com.shield.module.user.repository.UserRepository;
import com.shield.tenant.context.TenantContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserResponse create(UserCreateRequest request) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        UserEntity saved = createUserEntity(request, tenantId);
        return userMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> list(Pageable pageable) {
        return PagedResponse.from(userRepository.findAllByDeletedFalse(pageable).map(userMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> listByUnit(UUID unitId, Pageable pageable) {
        return PagedResponse.from(userRepository.findAllByUnitIdAndDeletedFalse(unitId, pageable).map(userMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> listByRole(UserRole role, Pageable pageable) {
        return PagedResponse.from(userRepository.findAllByRoleAndDeletedFalse(role, pageable).map(userMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        UserEntity user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return userMapper.toResponse(user);
    }

    public UserResponse update(UUID id, UserUpdateRequest request) {
        UserEntity entity = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        entity.setUnitId(request.unitId());
        entity.setName(request.name());
        entity.setEmail(request.email().toLowerCase());
        entity.setPhone(request.phone());
        entity.setRole(request.role());
        entity.setStatus(request.status());

        UserEntity saved = userRepository.save(entity);
        auditLogService.record(saved.getTenantId(), saved.getId(), "USER_UPDATED", "users", saved.getId(), null);
        return userMapper.toResponse(saved);
    }

    public void delete(UUID id) {
        UserEntity entity = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        entity.setDeleted(true);
        userRepository.save(entity);
        auditLogService.record(entity.getTenantId(), entity.getId(), "USER_DELETED", "users", entity.getId(), null);
    }

    public UserBulkImportResponse bulkImport(UserBulkImportRequest request) {
        UUID tenantId = TenantContext.getRequiredTenantId();
        List<String> errors = new ArrayList<>();
        int createdCount = 0;

        for (int i = 0; i < request.users().size(); i++) {
            UserCreateRequest userRequest = request.users().get(i);
            try {
                createUserEntity(userRequest, tenantId);
                createdCount++;
            } catch (RuntimeException ex) {
                errors.add("Row " + (i + 1) + ": " + ex.getMessage());
            }
        }

        return new UserBulkImportResponse(request.users().size(), createdCount, List.copyOf(errors));
    }

    @Transactional(readOnly = true)
    public String exportCsv() {
        List<UserEntity> users = userRepository.findAllByDeletedFalseOrderByCreatedAtDesc();
        StringBuilder csv = new StringBuilder();
        csv.append("id,tenantId,unitId,name,email,phone,role,status,createdAt\n");
        for (UserEntity user : users) {
            csv.append(csv(user.getId())).append(',')
                    .append(csv(user.getTenantId())).append(',')
                    .append(csv(user.getUnitId())).append(',')
                    .append(csv(user.getName())).append(',')
                    .append(csv(user.getEmail())).append(',')
                    .append(csv(user.getPhone())).append(',')
                    .append(csv(user.getRole())).append(',')
                    .append(csv(user.getStatus())).append(',')
                    .append(csv(user.getCreatedAt()))
                    .append('\n');
        }
        return csv.toString();
    }

    private UserEntity createUserEntity(UserCreateRequest request, UUID tenantId) {
        if (userRepository.existsByTenantIdAndEmailIgnoreCaseAndDeletedFalse(tenantId, request.email())) {
            throw new BadRequestException("Email already exists in tenant");
        }

        UserEntity entity = new UserEntity();
        entity.setTenantId(tenantId);
        entity.setUnitId(request.unitId());
        entity.setName(request.name());
        entity.setEmail(request.email().toLowerCase());
        entity.setPhone(request.phone());
        entity.setPasswordHash(passwordEncoder.encode(request.password()));
        entity.setRole(request.role());
        entity.setStatus(UserStatus.ACTIVE);

        UserEntity saved = userRepository.save(entity);
        auditLogService.record(tenantId, saved.getId(), "USER_CREATED", "users", saved.getId(), null);
        return saved;
    }

    private String csv(Object value) {
        if (value == null) {
            return "\"\"";
        }
        String text = value.toString().replace("\"", "\"\"");
        return "\"" + text + "\"";
    }
}
