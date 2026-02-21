package com.shield.module.emergency.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.emergency.dto.EmergencyContactCreateRequest;
import com.shield.module.emergency.dto.EmergencyContactOrderUpdateRequest;
import com.shield.module.emergency.dto.EmergencyContactResponse;
import com.shield.module.emergency.dto.EmergencyContactUpdateRequest;
import com.shield.module.emergency.dto.FireDrillRecordCreateRequest;
import com.shield.module.emergency.dto.FireDrillRecordResponse;
import com.shield.module.emergency.dto.FireDrillRecordUpdateRequest;
import com.shield.module.emergency.dto.SafetyEquipmentCreateRequest;
import com.shield.module.emergency.dto.SafetyEquipmentResponse;
import com.shield.module.emergency.dto.SafetyEquipmentUpdateRequest;
import com.shield.module.emergency.dto.SafetyInspectionCreateRequest;
import com.shield.module.emergency.dto.SafetyInspectionResponse;
import com.shield.module.emergency.dto.SafetyInspectionUpdateRequest;
import com.shield.module.emergency.dto.SosAlertRaiseRequest;
import com.shield.module.emergency.dto.SosAlertResolveRequest;
import com.shield.module.emergency.dto.SosAlertResponse;
import com.shield.module.emergency.dto.SosAlertResponseRequest;
import com.shield.module.emergency.entity.EmergencyContactEntity;
import com.shield.module.emergency.entity.FireDrillRecordEntity;
import com.shield.module.emergency.entity.SafetyEquipmentEntity;
import com.shield.module.emergency.entity.SafetyInspectionEntity;
import com.shield.module.emergency.entity.SosAlertEntity;
import com.shield.module.emergency.entity.SosAlertStatus;
import com.shield.module.emergency.repository.EmergencyContactRepository;
import com.shield.module.emergency.repository.FireDrillRecordRepository;
import com.shield.module.emergency.repository.SafetyEquipmentRepository;
import com.shield.module.emergency.repository.SafetyInspectionRepository;
import com.shield.module.emergency.repository.SosAlertRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmergencyService {

    private final EmergencyContactRepository emergencyContactRepository;
    private final SosAlertRepository sosAlertRepository;
    private final FireDrillRecordRepository fireDrillRecordRepository;
    private final SafetyEquipmentRepository safetyEquipmentRepository;
    private final SafetyInspectionRepository safetyInspectionRepository;
    private final AuditLogService auditLogService;

    public EmergencyService(
            EmergencyContactRepository emergencyContactRepository,
            SosAlertRepository sosAlertRepository,
            FireDrillRecordRepository fireDrillRecordRepository,
            SafetyEquipmentRepository safetyEquipmentRepository,
            SafetyInspectionRepository safetyInspectionRepository,
            AuditLogService auditLogService) {
        this.emergencyContactRepository = emergencyContactRepository;
        this.sosAlertRepository = sosAlertRepository;
        this.fireDrillRecordRepository = fireDrillRecordRepository;
        this.safetyEquipmentRepository = safetyEquipmentRepository;
        this.safetyInspectionRepository = safetyInspectionRepository;
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
    public PagedResponse<EmergencyContactResponse> listContactsByType(String contactType, Pageable pageable) {
        return PagedResponse.from(emergencyContactRepository.findAllByContactTypeIgnoreCaseAndDeletedFalse(contactType, pageable)
                .map(this::toContactResponse));
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

    public EmergencyContactResponse updateContactOrder(UUID id, EmergencyContactOrderUpdateRequest request, ShieldPrincipal principal) {
        EmergencyContactEntity entity = emergencyContactRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency contact not found: " + id));

        entity.setDisplayOrder(request.displayOrder());
        EmergencyContactEntity saved = emergencyContactRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "EMERGENCY_CONTACT_ORDER_UPDATED", "emergency_contact", saved.getId(), null);
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
    public PagedResponse<SosAlertResponse> listAlertsByType(String alertType, Pageable pageable) {
        return PagedResponse.from(sosAlertRepository.findAllByAlertTypeIgnoreCaseAndDeletedFalse(alertType, pageable).map(this::toAlertResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<SosAlertResponse> listAlertsByDateRange(Instant from, Instant to, Pageable pageable) {
        validateInstantRange(from, to);
        return PagedResponse.from(sosAlertRepository.findAllByCreatedAtBetweenAndDeletedFalse(from, to, pageable).map(this::toAlertResponse));
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

    public SosAlertResponse markFalseAlarm(UUID id, ShieldPrincipal principal) {
        SosAlertEntity entity = sosAlertRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("SOS alert not found: " + id));

        entity.setStatus(SosAlertStatus.FALSE_ALARM);
        entity.setResolvedAt(Instant.now());

        SosAlertEntity saved = sosAlertRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SOS_ALERT_FALSE_ALARM_MARKED", "sos_alert", saved.getId(), null);
        return toAlertResponse(saved);
    }

    public FireDrillRecordResponse createFireDrill(FireDrillRecordCreateRequest request, ShieldPrincipal principal) {
        FireDrillRecordEntity entity = new FireDrillRecordEntity();
        entity.setTenantId(principal.tenantId());
        entity.setDrillDate(request.drillDate());
        entity.setDrillTime(request.drillTime());
        entity.setConductedBy(request.conductedBy());
        entity.setEvacuationTime(request.evacuationTime());
        entity.setParticipantsCount(request.participantsCount());
        entity.setObservations(request.observations());
        entity.setReportUrl(request.reportUrl());

        FireDrillRecordEntity saved = fireDrillRecordRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "FIRE_DRILL_CREATED", "fire_drill_record", saved.getId(), null);
        return toFireDrillResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<FireDrillRecordResponse> listFireDrills(Pageable pageable) {
        return PagedResponse.from(fireDrillRecordRepository.findAllByDeletedFalse(pageable).map(this::toFireDrillResponse));
    }

    @Transactional(readOnly = true)
    public FireDrillRecordResponse getFireDrill(UUID id) {
        FireDrillRecordEntity entity = fireDrillRecordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fire drill record not found: " + id));
        return toFireDrillResponse(entity);
    }

    public FireDrillRecordResponse updateFireDrill(UUID id, FireDrillRecordUpdateRequest request, ShieldPrincipal principal) {
        FireDrillRecordEntity entity = fireDrillRecordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fire drill record not found: " + id));

        entity.setDrillDate(request.drillDate());
        entity.setDrillTime(request.drillTime());
        entity.setConductedBy(request.conductedBy());
        entity.setEvacuationTime(request.evacuationTime());
        entity.setParticipantsCount(request.participantsCount());
        entity.setObservations(request.observations());
        entity.setReportUrl(request.reportUrl());

        FireDrillRecordEntity saved = fireDrillRecordRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "FIRE_DRILL_UPDATED", "fire_drill_record", saved.getId(), null);
        return toFireDrillResponse(saved);
    }

    public void deleteFireDrill(UUID id, ShieldPrincipal principal) {
        FireDrillRecordEntity entity = fireDrillRecordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fire drill record not found: " + id));

        entity.setDeleted(true);
        fireDrillRecordRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "FIRE_DRILL_DELETED", "fire_drill_record", entity.getId(), null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<FireDrillRecordResponse> listFireDrillsByYear(int year, Pageable pageable) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);
        return PagedResponse.from(fireDrillRecordRepository.findAllByDrillDateBetweenAndDeletedFalse(from, to, pageable)
                .map(this::toFireDrillResponse));
    }

    public SafetyEquipmentResponse createSafetyEquipment(SafetyEquipmentCreateRequest request, ShieldPrincipal principal) {
        SafetyEquipmentEntity entity = new SafetyEquipmentEntity();
        entity.setTenantId(principal.tenantId());
        entity.setEquipmentType(request.equipmentType());
        entity.setEquipmentTag(request.equipmentTag());
        entity.setLocation(request.location());
        entity.setInstallationDate(request.installationDate());
        entity.setLastInspectionDate(request.lastInspectionDate());
        entity.setNextInspectionDate(request.nextInspectionDate());
        entity.setInspectionFrequencyDays(request.inspectionFrequencyDays());
        entity.setFunctional(request.functional() == null || request.functional());

        SafetyEquipmentEntity saved = safetyEquipmentRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SAFETY_EQUIPMENT_CREATED", "safety_equipment", saved.getId(), null);
        return toSafetyEquipmentResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SafetyEquipmentResponse> listSafetyEquipment(Pageable pageable) {
        return PagedResponse.from(safetyEquipmentRepository.findAllByDeletedFalse(pageable).map(this::toSafetyEquipmentResponse));
    }

    @Transactional(readOnly = true)
    public SafetyEquipmentResponse getSafetyEquipment(UUID id) {
        SafetyEquipmentEntity entity = safetyEquipmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Safety equipment not found: " + id));
        return toSafetyEquipmentResponse(entity);
    }

    public SafetyEquipmentResponse updateSafetyEquipment(UUID id, SafetyEquipmentUpdateRequest request, ShieldPrincipal principal) {
        SafetyEquipmentEntity entity = safetyEquipmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Safety equipment not found: " + id));

        entity.setEquipmentType(request.equipmentType());
        entity.setEquipmentTag(request.equipmentTag());
        entity.setLocation(request.location());
        entity.setInstallationDate(request.installationDate());
        entity.setLastInspectionDate(request.lastInspectionDate());
        entity.setNextInspectionDate(request.nextInspectionDate());
        entity.setInspectionFrequencyDays(request.inspectionFrequencyDays());
        entity.setFunctional(request.functional() == null || request.functional());

        SafetyEquipmentEntity saved = safetyEquipmentRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SAFETY_EQUIPMENT_UPDATED", "safety_equipment", saved.getId(), null);
        return toSafetyEquipmentResponse(saved);
    }

    public void deleteSafetyEquipment(UUID id, ShieldPrincipal principal) {
        SafetyEquipmentEntity entity = safetyEquipmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Safety equipment not found: " + id));

        entity.setDeleted(true);
        safetyEquipmentRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SAFETY_EQUIPMENT_DELETED", "safety_equipment", entity.getId(), null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SafetyEquipmentResponse> listSafetyEquipmentByType(String type, Pageable pageable) {
        return PagedResponse.from(safetyEquipmentRepository.findAllByEquipmentTypeIgnoreCaseAndDeletedFalse(type, pageable)
                .map(this::toSafetyEquipmentResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<SafetyEquipmentResponse> listSafetyEquipmentInspectionDue(LocalDate date, Pageable pageable) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return PagedResponse.from(safetyEquipmentRepository.findAllByNextInspectionDateLessThanEqualAndDeletedFalse(targetDate, pageable)
                .map(this::toSafetyEquipmentResponse));
    }

    public SafetyInspectionResponse createSafetyInspection(SafetyInspectionCreateRequest request, ShieldPrincipal principal) {
        safetyEquipmentRepository.findByIdAndDeletedFalse(request.equipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Safety equipment not found: " + request.equipmentId()));

        SafetyInspectionEntity entity = new SafetyInspectionEntity();
        entity.setTenantId(principal.tenantId());
        entity.setEquipmentId(request.equipmentId());
        entity.setInspectionDate(request.inspectionDate());
        entity.setInspectedBy(request.inspectedBy() != null ? request.inspectedBy() : principal.userId());
        entity.setInspectionResult(request.inspectionResult());
        entity.setRemarks(request.remarks());

        SafetyInspectionEntity saved = safetyInspectionRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SAFETY_INSPECTION_CREATED", "safety_inspection", saved.getId(), null);
        return toSafetyInspectionResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SafetyInspectionResponse> listSafetyInspections(Pageable pageable) {
        return PagedResponse.from(safetyInspectionRepository.findAllByDeletedFalse(pageable).map(this::toSafetyInspectionResponse));
    }

    @Transactional(readOnly = true)
    public SafetyInspectionResponse getSafetyInspection(UUID id) {
        SafetyInspectionEntity entity = safetyInspectionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Safety inspection not found: " + id));
        return toSafetyInspectionResponse(entity);
    }

    public SafetyInspectionResponse updateSafetyInspection(UUID id, SafetyInspectionUpdateRequest request, ShieldPrincipal principal) {
        safetyEquipmentRepository.findByIdAndDeletedFalse(request.equipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Safety equipment not found: " + request.equipmentId()));

        SafetyInspectionEntity entity = safetyInspectionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Safety inspection not found: " + id));

        entity.setEquipmentId(request.equipmentId());
        entity.setInspectionDate(request.inspectionDate());
        entity.setInspectedBy(request.inspectedBy() != null ? request.inspectedBy() : principal.userId());
        entity.setInspectionResult(request.inspectionResult());
        entity.setRemarks(request.remarks());

        SafetyInspectionEntity saved = safetyInspectionRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SAFETY_INSPECTION_UPDATED", "safety_inspection", saved.getId(), null);
        return toSafetyInspectionResponse(saved);
    }

    public void deleteSafetyInspection(UUID id, ShieldPrincipal principal) {
        SafetyInspectionEntity entity = safetyInspectionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Safety inspection not found: " + id));

        entity.setDeleted(true);
        safetyInspectionRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "SAFETY_INSPECTION_DELETED", "safety_inspection", entity.getId(), null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SafetyInspectionResponse> listSafetyInspectionsByEquipment(UUID equipmentId, Pageable pageable) {
        return PagedResponse.from(safetyInspectionRepository.findAllByEquipmentIdAndDeletedFalse(equipmentId, pageable)
                .map(this::toSafetyInspectionResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<SafetyInspectionResponse> listSafetyInspectionsByDateRange(LocalDate from, LocalDate to, Pageable pageable) {
        validateLocalDateRange(from, to);
        return PagedResponse.from(safetyInspectionRepository.findAllByInspectionDateBetweenAndDeletedFalse(from, to, pageable)
                .map(this::toSafetyInspectionResponse));
    }

    private void validateInstantRange(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new BadRequestException("Both from and to date-time values are required");
        }
        if (to.isBefore(from)) {
            throw new BadRequestException("to date-time cannot be before from date-time");
        }
    }

    private void validateLocalDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BadRequestException("Both from and to dates are required");
        }
        if (to.isBefore(from)) {
            throw new BadRequestException("to date cannot be before from date");
        }
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

    private FireDrillRecordResponse toFireDrillResponse(FireDrillRecordEntity entity) {
        return new FireDrillRecordResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getDrillDate(),
                entity.getDrillTime(),
                entity.getConductedBy(),
                entity.getEvacuationTime(),
                entity.getParticipantsCount(),
                entity.getObservations(),
                entity.getReportUrl());
    }

    private SafetyEquipmentResponse toSafetyEquipmentResponse(SafetyEquipmentEntity entity) {
        return new SafetyEquipmentResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getEquipmentType(),
                entity.getEquipmentTag(),
                entity.getLocation(),
                entity.getInstallationDate(),
                entity.getLastInspectionDate(),
                entity.getNextInspectionDate(),
                entity.getInspectionFrequencyDays(),
                entity.isFunctional());
    }

    private SafetyInspectionResponse toSafetyInspectionResponse(SafetyInspectionEntity entity) {
        return new SafetyInspectionResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getEquipmentId(),
                entity.getInspectionDate(),
                entity.getInspectedBy(),
                entity.getInspectionResult(),
                entity.getRemarks());
    }
}
