package com.shield.module.emergency.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.emergency.dto.EmergencyContactCreateRequest;
import com.shield.module.emergency.dto.EmergencyContactResponse;
import com.shield.module.emergency.dto.EmergencyContactUpdateRequest;
import com.shield.module.emergency.dto.SosAlertRaiseRequest;
import com.shield.module.emergency.dto.SosAlertResolveRequest;
import com.shield.module.emergency.dto.SosAlertResponse;
import com.shield.module.emergency.dto.SosAlertResponseRequest;
import com.shield.module.emergency.entity.EmergencyContactEntity;
import com.shield.module.emergency.entity.SosAlertEntity;
import com.shield.module.emergency.entity.SosAlertStatus;
import com.shield.module.emergency.repository.EmergencyContactRepository;
import com.shield.module.emergency.repository.SosAlertRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmergencyService {

    private final EmergencyContactRepository emergencyContactRepository;
    private final SosAlertRepository sosAlertRepository;
    private final AuditLogService auditLogService;

    public EmergencyService(
            EmergencyContactRepository emergencyContactRepository,
            SosAlertRepository sosAlertRepository,
            AuditLogService auditLogService) {
        this.emergencyContactRepository = emergencyContactRepository;
        this.sosAlertRepository = sosAlertRepository;
        this.auditLogService = auditLogService;
    }

    public EmergencyContactResponse createContact(EmergencyContactCreateRequest request, ShieldPrincipal principal) {
        EmergencyContactEntity entity = new EmergencyContactEntity();
        entity.setTenantId(principal.tenantId());
        entity.setContactType(request.contactType());
        entity.setContactName(request.contactName());
        entity.setPhonePrimary(request.phonePrimary());
        entity.setPhoneSecondary(request.phoneSecondary());
        entity.setAddress(request.address());
        entity.setDisplayOrder(request.displayOrder());
        entity.setActive(request.active());

        EmergencyContactEntity saved = emergencyContactRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "EMERGENCY_CONTACT_CREATED", "emergency_contact", saved.getId(), null);
        return toContactResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<EmergencyContactResponse> listContacts(Pageable pageable) {
        return PagedResponse.from(emergencyContactRepository.findAllByDeletedFalse(pageable).map(this::toContactResponse));
    }

    @Transactional(readOnly = true)
    public EmergencyContactResponse getContact(UUID id) {
        EmergencyContactEntity entity = emergencyContactRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency contact not found: " + id));
        return toContactResponse(entity);
    }

    public EmergencyContactResponse updateContact(UUID id, EmergencyContactUpdateRequest request, ShieldPrincipal principal) {
        EmergencyContactEntity entity = emergencyContactRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency contact not found: " + id));

        entity.setContactType(request.contactType());
        entity.setContactName(request.contactName());
        entity.setPhonePrimary(request.phonePrimary());
        entity.setPhoneSecondary(request.phoneSecondary());
        entity.setAddress(request.address());
        entity.setDisplayOrder(request.displayOrder());
        entity.setActive(request.active());

        EmergencyContactEntity saved = emergencyContactRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "EMERGENCY_CONTACT_UPDATED", "emergency_contact", saved.getId(), null);
        return toContactResponse(saved);
    }

    public void deleteContact(UUID id, ShieldPrincipal principal) {
        EmergencyContactEntity entity = emergencyContactRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency contact not found: " + id));

        entity.setDeleted(true);
        emergencyContactRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "EMERGENCY_CONTACT_DELETED", "emergency_contact", entity.getId(), null);
    }

    public SosAlertResponse raiseAlert(SosAlertRaiseRequest request, ShieldPrincipal principal) {
        SosAlertEntity entity = new SosAlertEntity();
        entity.setTenantId(principal.tenantId());
        entity.setAlertNumber("SOS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        entity.setRaisedBy(principal.userId());
        entity.setUnitId(request.unitId());
        entity.setAlertType(request.alertType());
        entity.setLocation(request.location());
        entity.setDescription(request.description());
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        entity.setStatus(SosAlertStatus.ACTIVE);

        SosAlertEntity saved = sosAlertRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SOS_ALERT_RAISED", "sos_alert", saved.getId(), null);
        return toAlertResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SosAlertResponse> listAlerts(Pageable pageable) {
        return PagedResponse.from(sosAlertRepository.findAllByDeletedFalse(pageable).map(this::toAlertResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<SosAlertResponse> listActiveAlerts(Pageable pageable) {
        return PagedResponse.from(sosAlertRepository.findAllByStatusAndDeletedFalse(SosAlertStatus.ACTIVE, pageable).map(this::toAlertResponse));
    }

    @Transactional(readOnly = true)
    public SosAlertResponse getAlert(UUID id) {
        SosAlertEntity entity = sosAlertRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SOS alert not found: " + id));
        return toAlertResponse(entity);
    }

    public SosAlertResponse respondAlert(UUID id, SosAlertResponseRequest request, ShieldPrincipal principal) {
        SosAlertEntity entity = sosAlertRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SOS alert not found: " + id));

        entity.setStatus(SosAlertStatus.RESPONDED);
        entity.setRespondedBy(request.respondedBy() != null ? request.respondedBy() : principal.userId());
        entity.setRespondedAt(Instant.now());

        SosAlertEntity saved = sosAlertRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SOS_ALERT_RESPONDED", "sos_alert", saved.getId(), null);
        return toAlertResponse(saved);
    }

    public SosAlertResponse resolveAlert(UUID id, SosAlertResolveRequest request, ShieldPrincipal principal) {
        SosAlertEntity entity = sosAlertRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SOS alert not found: " + id));

        entity.setStatus(request.falseAlarm() ? SosAlertStatus.FALSE_ALARM : SosAlertStatus.RESOLVED);
        entity.setResolvedAt(Instant.now());

        SosAlertEntity saved = sosAlertRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SOS_ALERT_RESOLVED", "sos_alert", saved.getId(), null);
        return toAlertResponse(saved);
    }

    private EmergencyContactResponse toContactResponse(EmergencyContactEntity entity) {
        return new EmergencyContactResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getContactType(),
                entity.getContactName(),
                entity.getPhonePrimary(),
                entity.getPhoneSecondary(),
                entity.getAddress(),
                entity.getDisplayOrder(),
                entity.isActive());
    }

    private SosAlertResponse toAlertResponse(SosAlertEntity entity) {
        return new SosAlertResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getAlertNumber(),
                entity.getRaisedBy(),
                entity.getUnitId(),
                entity.getAlertType(),
                entity.getLocation(),
                entity.getDescription(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getStatus(),
                entity.getRespondedBy(),
                entity.getRespondedAt(),
                entity.getResolvedAt());
    }
}
