package com.shield.module.visitor.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.common.util.SecurityUtils;
import com.shield.module.visitor.dto.BlacklistCheckResponse;
import com.shield.module.visitor.dto.BlacklistCreateRequest;
import com.shield.module.visitor.dto.BlacklistResponse;
import com.shield.module.visitor.dto.BlacklistUpdateRequest;
import com.shield.module.visitor.dto.DeliveryLogCreateRequest;
import com.shield.module.visitor.dto.DeliveryLogResponse;
import com.shield.module.visitor.dto.DomesticHelpAssignUnitRequest;
import com.shield.module.visitor.dto.DomesticHelpResponse;
import com.shield.module.visitor.dto.DomesticHelpCreateRequest;
import com.shield.module.visitor.dto.DomesticHelpUnitMappingResponse;
import com.shield.module.visitor.dto.DomesticHelpUpdateRequest;
import com.shield.module.visitor.dto.VisitorCreateRequest;
import com.shield.module.visitor.dto.VisitorLogEntryRequest;
import com.shield.module.visitor.dto.VisitorLogExitRequest;
import com.shield.module.visitor.dto.VisitorLogResponse;
import com.shield.module.visitor.dto.VisitorPassCreateRequest;
import com.shield.module.visitor.dto.VisitorPassPreApproveRequest;
import com.shield.module.visitor.dto.VisitorPassResponse;
import com.shield.module.visitor.dto.VisitorPassUpdateRequest;
import com.shield.module.visitor.dto.VisitorResponse;
import com.shield.module.visitor.dto.VisitorUpdateRequest;
import com.shield.module.visitor.entity.BlacklistEntity;
import com.shield.module.visitor.entity.DeliveryLogEntity;
import com.shield.module.visitor.entity.DomesticHelpEntity;
import com.shield.module.visitor.entity.DomesticHelpUnitMappingEntity;
import com.shield.module.visitor.entity.VisitorEntity;
import com.shield.module.visitor.entity.VisitorEntryExitLogEntity;
import com.shield.module.visitor.entity.VisitorPassEntity;
import com.shield.module.visitor.entity.VisitorPassStatus;
import com.shield.module.visitor.repository.BlacklistRepository;
import com.shield.module.visitor.repository.DeliveryLogRepository;
import com.shield.module.visitor.repository.DomesticHelpRepository;
import com.shield.module.visitor.repository.DomesticHelpUnitMappingRepository;
import com.shield.module.visitor.repository.VisitorEntryExitLogRepository;
import com.shield.module.visitor.repository.VisitorPassRepository;
import com.shield.module.visitor.repository.VisitorRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VisitorService {

    private final VisitorPassRepository visitorPassRepository;
    private final VisitorRepository visitorRepository;
    private final VisitorEntryExitLogRepository visitorEntryExitLogRepository;
    private final DomesticHelpRepository domesticHelpRepository;
    private final DomesticHelpUnitMappingRepository domesticHelpUnitMappingRepository;
    private final BlacklistRepository blacklistRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final AuditLogService auditLogService;

    // Legacy endpoints compatibility: /api/v1/visitors/pass/*
    public VisitorPassResponse createPass(VisitorPassCreateRequest request) {
        return createPass(request, SecurityUtils.getCurrentPrincipal());
    }

    @Transactional(readOnly = true)
    public VisitorPassResponse getPass(UUID id) {
        VisitorPassEntity pass = visitorPassRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor pass not found: " + id));
        return toPassResponse(pass);
    }

    public VisitorPassResponse approve(UUID id) {
        return updatePassStatus(id, VisitorPassStatus.APPROVED, "VISITOR_PASS_APPROVED", SecurityUtils.getCurrentPrincipal().userId());
    }

    public VisitorPassResponse reject(UUID id) {
        return updatePassStatus(id, VisitorPassStatus.REJECTED, "VISITOR_PASS_REJECTED", SecurityUtils.getCurrentPrincipal().userId());
    }

    public VisitorPassResponse createPass(VisitorPassCreateRequest request, ShieldPrincipal principal) {
        VisitorPassEntity pass = buildPassFromCreateRequest(request, principal.tenantId(), VisitorPassStatus.PENDING, null);
        VisitorPassEntity saved = visitorPassRepository.save(pass);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "VISITOR_PASS_CREATED", "visitor_pass", saved.getId(), null);
        return toPassResponse(saved);
    }

    public VisitorPassResponse preApprovePass(VisitorPassPreApproveRequest request, ShieldPrincipal principal) {
        VisitorPassCreateRequest createRequest = new VisitorPassCreateRequest(
                request.visitorId(),
                request.unitId(),
                request.visitorName(),
                request.vehicleNumber(),
                request.validFrom(),
                request.validTo(),
                request.visitDate(),
                request.purpose(),
                request.numberOfPersons());

        UUID approvedBy = request.approvedBy() != null ? request.approvedBy() : principal.userId();
        VisitorPassEntity pass = buildPassFromCreateRequest(createRequest, principal.tenantId(), VisitorPassStatus.ACTIVE, approvedBy);
        VisitorPassEntity saved = visitorPassRepository.save(pass);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "VISITOR_PASS_PREAPPROVED", "visitor_pass", saved.getId(), null);
        return toPassResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<VisitorPassResponse> listPasses(Pageable pageable) {
        return PagedResponse.from(visitorPassRepository.findAllByDeletedFalse(pageable).map(this::toPassResponse));
    }

    public VisitorPassResponse updatePass(UUID id, VisitorPassUpdateRequest request, ShieldPrincipal principal) {
        VisitorPassEntity pass = getPassEntity(id);

        pass.setVisitorId(request.visitorId());
        pass.setUnitId(request.unitId());
        pass.setVisitorName(resolveVisitorName(request.visitorName(), request.visitorId()));
        pass.setVehicleNumber(resolveVehicleNumber(request.vehicleNumber(), request.visitorId()));
        pass.setValidFrom(request.validFrom());
        pass.setValidTo(request.validTo());
        pass.setVisitDate(resolveVisitDate(request.visitDate(), request.validFrom()));
        pass.setPurpose(request.purpose());
        pass.setNumberOfPersons(resolveNumberOfPersons(request.numberOfPersons()));

        if (request.status() != null) {
            pass.setStatus(request.status());
            if (request.status() == VisitorPassStatus.APPROVED || request.status() == VisitorPassStatus.ACTIVE) {
                pass.setApprovedBy(principal.userId());
            }
        }

        VisitorPassEntity saved = visitorPassRepository.save(pass);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "VISITOR_PASS_UPDATED", "visitor_pass", saved.getId(), null);
        return toPassResponse(saved);
    }

    public void deletePass(UUID id, ShieldPrincipal principal) {
        VisitorPassEntity pass = getPassEntity(id);
        pass.setDeleted(true);
        visitorPassRepository.save(pass);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "VISITOR_PASS_DELETED", "visitor_pass", pass.getId(), null);
    }

    public VisitorPassResponse cancelPass(UUID id, ShieldPrincipal principal) {
        VisitorPassResponse response = updatePassStatus(id, VisitorPassStatus.CANCELLED, "VISITOR_PASS_CANCELLED", principal.userId());
        return response;
    }

    @Transactional(readOnly = true)
    public PagedResponse<VisitorPassResponse> listPassesByUnit(UUID unitId, Pageable pageable) {
        return PagedResponse.from(visitorPassRepository.findAllByUnitIdAndDeletedFalse(unitId, pageable).map(this::toPassResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<VisitorPassResponse> listPassesByDate(LocalDate date, Pageable pageable) {
        return PagedResponse.from(visitorPassRepository.findAllByVisitDateAndDeletedFalse(date, pageable).map(this::toPassResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<VisitorPassResponse> listActivePasses(Pageable pageable) {
        return PagedResponse.from(visitorPassRepository
                .findAllByStatusInAndValidToGreaterThanAndDeletedFalse(
                        List.of(VisitorPassStatus.ACTIVE, VisitorPassStatus.APPROVED),
                        Instant.now(),
                        pageable)
                .map(this::toPassResponse));
    }

    public VisitorPassResponse verifyPassByQrCode(String qrCode) {
        VisitorPassEntity pass = visitorPassRepository.findByQrCodeAndDeletedFalse(qrCode)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor pass not found for QR: " + qrCode));

        if (pass.getValidTo() != null && pass.getValidTo().isBefore(Instant.now()) && pass.getStatus() != VisitorPassStatus.EXPIRED) {
            pass.setStatus(VisitorPassStatus.EXPIRED);
            visitorPassRepository.save(pass);
        }

        return toPassResponse(pass);
    }

    public VisitorLogResponse logEntry(VisitorLogEntryRequest request, ShieldPrincipal principal) {
        VisitorPassEntity pass = getPassEntity(request.visitorPassId());

        VisitorEntryExitLogEntity log = new VisitorEntryExitLogEntity();
        log.setTenantId(principal.tenantId());
        log.setVisitorPassId(pass.getId());
        log.setEntryTime(Instant.now());
        log.setEntryGate(request.entryGate());
        log.setSecurityGuardEntry(request.securityGuardEntry() != null ? request.securityGuardEntry() : principal.userId());
        log.setFaceCaptureUrl(request.faceCaptureUrl());

        VisitorEntryExitLogEntity saved = visitorEntryExitLogRepository.save(log);

        if (pass.getStatus() == VisitorPassStatus.PENDING || pass.getStatus() == VisitorPassStatus.APPROVED) {
            pass.setStatus(VisitorPassStatus.ACTIVE);
            visitorPassRepository.save(pass);
        }

        auditLogService.logEvent(principal.tenantId(), principal.userId(), "VISITOR_ENTRY_LOGGED", "visitor_entry_exit_log", saved.getId(), null);
        return toVisitorLogResponse(saved);
    }

    public VisitorLogResponse logExit(VisitorLogExitRequest request, ShieldPrincipal principal) {
        VisitorEntryExitLogEntity log = visitorEntryExitLogRepository
                .findFirstByVisitorPassIdAndExitTimeIsNullAndDeletedFalseOrderByEntryTimeDesc(request.visitorPassId())
                .orElseThrow(() -> new ResourceNotFoundException("Open visitor log not found for pass: " + request.visitorPassId()));

        log.setExitTime(Instant.now());
        log.setExitGate(request.exitGate());
        log.setSecurityGuardExit(request.securityGuardExit() != null ? request.securityGuardExit() : principal.userId());

        VisitorEntryExitLogEntity saved = visitorEntryExitLogRepository.save(log);

        VisitorPassEntity pass = getPassEntity(request.visitorPassId());
        if (pass.getStatus() == VisitorPassStatus.ACTIVE || pass.getStatus() == VisitorPassStatus.APPROVED) {
            pass.setStatus(VisitorPassStatus.USED);
            visitorPassRepository.save(pass);
        }

        auditLogService.logEvent(principal.tenantId(), principal.userId(), "VISITOR_EXIT_LOGGED", "visitor_entry_exit_log", saved.getId(), null);
        return toVisitorLogResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<VisitorLogResponse> listVisitorLogs(Pageable pageable) {
        return PagedResponse.from(visitorEntryExitLogRepository.findAllByDeletedFalse(pageable).map(this::toVisitorLogResponse));
    }

    @Transactional(readOnly = true)
    public VisitorLogResponse getVisitorLog(UUID id) {
        VisitorEntryExitLogEntity log = visitorEntryExitLogRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor log not found: " + id));
        return toVisitorLogResponse(log);
    }

    @Transactional(readOnly = true)
    public PagedResponse<VisitorLogResponse> listVisitorLogsByPass(UUID passId, Pageable pageable) {
        return PagedResponse.from(visitorEntryExitLogRepository.findAllByVisitorPassIdAndDeletedFalse(passId, pageable)
                .map(this::toVisitorLogResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<VisitorLogResponse> listVisitorLogsByDateRange(Instant from, Instant to, Pageable pageable) {
        if (from.isAfter(to)) {
            throw new BadRequestException("from must be before to");
        }
        return PagedResponse.from(visitorEntryExitLogRepository.findAllByEntryTimeBetweenAndDeletedFalse(from, to, pageable)
                .map(this::toVisitorLogResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<VisitorLogResponse> listCurrentlyInside(Pageable pageable) {
        return PagedResponse.from(visitorEntryExitLogRepository.findAllByExitTimeIsNullAndDeletedFalse(pageable)
                .map(this::toVisitorLogResponse));
    }

    public VisitorResponse createVisitor(VisitorCreateRequest request, ShieldPrincipal principal) {
        VisitorEntity entity = new VisitorEntity();
        entity.setTenantId(principal.tenantId());
        entity.setVisitorName(request.visitorName());
        entity.setPhone(request.phone());
        entity.setVehicleNumber(request.vehicleNumber());
        entity.setVisitorType(request.visitorType());
        entity.setIdProofType(request.idProofType());
        entity.setIdProofNumber(request.idProofNumber());
        entity.setPhotoUrl(request.photoUrl());

        VisitorEntity saved = visitorRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "VISITOR_CREATED", "visitor", saved.getId(), null);
        return toVisitorResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<VisitorResponse> listVisitors(Pageable pageable) {
        return PagedResponse.from(visitorRepository.findAllByDeletedFalse(pageable).map(this::toVisitorResponse));
    }

    @Transactional(readOnly = true)
    public VisitorResponse getVisitor(UUID id) {
        return toVisitorResponse(getVisitorEntity(id));
    }

    public VisitorResponse updateVisitor(UUID id, VisitorUpdateRequest request, ShieldPrincipal principal) {
        VisitorEntity entity = getVisitorEntity(id);
        entity.setVisitorName(request.visitorName());
        entity.setPhone(request.phone());
        entity.setVehicleNumber(request.vehicleNumber());
        entity.setVisitorType(request.visitorType());
        entity.setIdProofType(request.idProofType());
        entity.setIdProofNumber(request.idProofNumber());
        entity.setPhotoUrl(request.photoUrl());

        VisitorEntity saved = visitorRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "VISITOR_UPDATED", "visitor", saved.getId(), null);
        return toVisitorResponse(saved);
    }

    public void deleteVisitor(UUID id, ShieldPrincipal principal) {
        VisitorEntity entity = getVisitorEntity(id);
        entity.setDeleted(true);
        visitorRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "VISITOR_DELETED", "visitor", entity.getId(), null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<VisitorResponse> searchVisitors(String query, Pageable pageable) {
        String safeQuery = query == null ? "" : query.trim();
        if (safeQuery.isEmpty()) {
            return listVisitors(pageable);
        }
        return PagedResponse.from(visitorRepository.search(safeQuery, pageable).map(this::toVisitorResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<VisitorResponse> listVisitorsByPhone(String phone, Pageable pageable) {
        return PagedResponse.from(visitorRepository.findAllByPhoneAndDeletedFalse(phone, pageable).map(this::toVisitorResponse));
    }

    public DomesticHelpResponse createDomesticHelp(DomesticHelpCreateRequest request, ShieldPrincipal principal) {
        DomesticHelpEntity entity = new DomesticHelpEntity();
        entity.setTenantId(principal.tenantId());
        entity.setHelpName(request.helpName());
        entity.setPhone(request.phone());
        entity.setHelpType(request.helpType());
        entity.setPermanentPass(Boolean.TRUE.equals(request.permanentPass()));
        entity.setPoliceVerificationDone(Boolean.TRUE.equals(request.policeVerificationDone()));
        entity.setVerificationDate(request.verificationDate());
        entity.setPhotoUrl(request.photoUrl());
        entity.setRegisteredBy(request.registeredBy() != null ? request.registeredBy() : principal.userId());

        DomesticHelpEntity saved = domesticHelpRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DOMESTIC_HELP_CREATED", "domestic_help_registry", saved.getId(), null);
        return toDomesticHelpResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DomesticHelpResponse> listDomesticHelp(Pageable pageable) {
        return PagedResponse.from(domesticHelpRepository.findAllByDeletedFalse(pageable).map(this::toDomesticHelpResponse));
    }

    @Transactional(readOnly = true)
    public DomesticHelpResponse getDomesticHelp(UUID id) {
        return toDomesticHelpResponse(getDomesticHelpEntity(id));
    }

    public DomesticHelpResponse updateDomesticHelp(UUID id, DomesticHelpUpdateRequest request, ShieldPrincipal principal) {
        DomesticHelpEntity entity = getDomesticHelpEntity(id);

        entity.setHelpName(request.helpName());
        entity.setPhone(request.phone());
        entity.setHelpType(request.helpType());
        entity.setPermanentPass(Boolean.TRUE.equals(request.permanentPass()));
        entity.setPoliceVerificationDone(Boolean.TRUE.equals(request.policeVerificationDone()));
        entity.setVerificationDate(request.verificationDate());
        entity.setPhotoUrl(request.photoUrl());

        DomesticHelpEntity saved = domesticHelpRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DOMESTIC_HELP_UPDATED", "domestic_help_registry", saved.getId(), null);
        return toDomesticHelpResponse(saved);
    }

    public void deleteDomesticHelp(UUID id, ShieldPrincipal principal) {
        DomesticHelpEntity entity = getDomesticHelpEntity(id);
        entity.setDeleted(true);
        domesticHelpRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DOMESTIC_HELP_DELETED", "domestic_help_registry", entity.getId(), null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DomesticHelpResponse> listDomesticHelpByType(String type, Pageable pageable) {
        return PagedResponse.from(domesticHelpRepository.findAllByHelpTypeAndDeletedFalse(type, pageable).map(this::toDomesticHelpResponse));
    }

    public DomesticHelpResponse verifyDomesticHelp(UUID id, ShieldPrincipal principal) {
        DomesticHelpEntity entity = getDomesticHelpEntity(id);
        entity.setPoliceVerificationDone(true);
        entity.setVerificationDate(LocalDate.now());

        DomesticHelpEntity saved = domesticHelpRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DOMESTIC_HELP_VERIFIED", "domestic_help_registry", saved.getId(), null);
        return toDomesticHelpResponse(saved);
    }

    public DomesticHelpUnitMappingResponse assignDomesticHelpToUnit(UUID domesticHelpId, DomesticHelpAssignUnitRequest request, ShieldPrincipal principal) {
        getDomesticHelpEntity(domesticHelpId);

        DomesticHelpUnitMappingEntity mapping = new DomesticHelpUnitMappingEntity();
        mapping.setTenantId(principal.tenantId());
        mapping.setDomesticHelpId(domesticHelpId);
        mapping.setUnitId(request.unitId());
        mapping.setStartDate(request.startDate() != null ? request.startDate() : LocalDate.now());
        mapping.setEndDate(request.endDate());
        mapping.setActive(true);

        DomesticHelpUnitMappingEntity saved = domesticHelpUnitMappingRepository.save(mapping);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DOMESTIC_HELP_ASSIGNED", "domestic_help_unit_mapping", saved.getId(), null);
        return toDomesticHelpUnitMappingResponse(saved);
    }

    public void removeDomesticHelpUnitMapping(UUID helpId, UUID unitId, ShieldPrincipal principal) {
        DomesticHelpUnitMappingEntity mapping = domesticHelpUnitMappingRepository
                .findFirstByDomesticHelpIdAndUnitIdAndActiveTrueAndDeletedFalse(helpId, unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Domestic help mapping not found for helpId=" + helpId + " unitId=" + unitId));

        mapping.setActive(false);
        mapping.setDeleted(true);
        domesticHelpUnitMappingRepository.save(mapping);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DOMESTIC_HELP_UNASSIGNED", "domestic_help_unit_mapping", mapping.getId(), null);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DomesticHelpResponse> listDomesticHelpByUnit(UUID unitId, Pageable pageable) {
        Page<DomesticHelpUnitMappingEntity> mappingPage = domesticHelpUnitMappingRepository
                .findAllByUnitIdAndActiveTrueAndDeletedFalse(unitId, pageable);

        return PagedResponse.from(mappingPage.map(mapping -> domesticHelpRepository.findByIdAndDeletedFalse(mapping.getDomesticHelpId())
                .map(this::toDomesticHelpResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Domestic help not found: " + mapping.getDomesticHelpId()))));
    }

    public BlacklistResponse createBlacklist(BlacklistCreateRequest request, ShieldPrincipal principal) {
        BlacklistEntity entity = new BlacklistEntity();
        entity.setTenantId(principal.tenantId());
        entity.setPersonName(request.personName());
        entity.setPhone(request.phone());
        entity.setReason(request.reason());
        entity.setBlacklistedBy(request.blacklistedBy() != null ? request.blacklistedBy() : principal.userId());
        entity.setBlacklistDate(LocalDate.now());
        entity.setActive(true);

        BlacklistEntity saved = blacklistRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "BLACKLIST_CREATED", "blacklist", saved.getId(), null);
        return toBlacklistResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<BlacklistResponse> listBlacklist(Pageable pageable) {
        return PagedResponse.from(blacklistRepository.findAllByDeletedFalse(pageable).map(this::toBlacklistResponse));
    }

    @Transactional(readOnly = true)
    public BlacklistResponse getBlacklist(UUID id) {
        return toBlacklistResponse(getBlacklistEntity(id));
    }

    public BlacklistResponse updateBlacklist(UUID id, BlacklistUpdateRequest request, ShieldPrincipal principal) {
        BlacklistEntity entity = getBlacklistEntity(id);
        entity.setPersonName(request.personName());
        entity.setPhone(request.phone());
        entity.setReason(request.reason());

        BlacklistEntity saved = blacklistRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "BLACKLIST_UPDATED", "blacklist", saved.getId(), null);
        return toBlacklistResponse(saved);
    }

    public void deleteBlacklist(UUID id, ShieldPrincipal principal) {
        BlacklistEntity entity = getBlacklistEntity(id);
        entity.setDeleted(true);
        blacklistRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "BLACKLIST_DELETED", "blacklist", entity.getId(), null);
    }

    @Transactional(readOnly = true)
    public BlacklistCheckResponse checkBlacklistByPhone(String phone) {
        boolean blacklisted = blacklistRepository.findByPhoneAndActiveTrueAndDeletedFalse(phone).isPresent();
        return new BlacklistCheckResponse(phone, blacklisted);
    }

    public BlacklistResponse activateBlacklist(UUID id, ShieldPrincipal principal) {
        BlacklistEntity entity = getBlacklistEntity(id);
        entity.setActive(true);
        BlacklistEntity saved = blacklistRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "BLACKLIST_ACTIVATED", "blacklist", saved.getId(), null);
        return toBlacklistResponse(saved);
    }

    public BlacklistResponse deactivateBlacklist(UUID id, ShieldPrincipal principal) {
        BlacklistEntity entity = getBlacklistEntity(id);
        entity.setActive(false);
        BlacklistEntity saved = blacklistRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "BLACKLIST_DEACTIVATED", "blacklist", saved.getId(), null);
        return toBlacklistResponse(saved);
    }

    public DeliveryLogResponse createDeliveryLog(DeliveryLogCreateRequest request, ShieldPrincipal principal) {
        DeliveryLogEntity entity = new DeliveryLogEntity();
        entity.setTenantId(principal.tenantId());
        entity.setUnitId(request.unitId());
        entity.setDeliveryPartner(request.deliveryPartner());
        entity.setTrackingNumber(request.trackingNumber());
        entity.setDeliveryTime(request.deliveryTime() != null ? request.deliveryTime() : Instant.now());
        entity.setReceivedBy(request.receivedBy());
        entity.setSecurityGuardId(request.securityGuardId() != null ? request.securityGuardId() : principal.userId());
        entity.setPhotoUrl(request.photoUrl());

        DeliveryLogEntity saved = deliveryLogRepository.save(entity);
        auditLogService.logEvent(principal.tenantId(), principal.userId(), "DELIVERY_LOG_CREATED", "delivery_log", saved.getId(), null);
        return toDeliveryLogResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DeliveryLogResponse> listDeliveryLogs(Pageable pageable) {
        return PagedResponse.from(deliveryLogRepository.findAllByDeletedFalse(pageable).map(this::toDeliveryLogResponse));
    }

    @Transactional(readOnly = true)
    public DeliveryLogResponse getDeliveryLog(UUID id) {
        DeliveryLogEntity entity = deliveryLogRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery log not found: " + id));
        return toDeliveryLogResponse(entity);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DeliveryLogResponse> listDeliveryLogsByUnit(UUID unitId, Pageable pageable) {
        return PagedResponse.from(deliveryLogRepository.findAllByUnitIdAndDeletedFalse(unitId, pageable).map(this::toDeliveryLogResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<DeliveryLogResponse> listDeliveryLogsByPartner(String partner, Pageable pageable) {
        return PagedResponse.from(deliveryLogRepository.findAllByDeliveryPartnerIgnoreCaseAndDeletedFalse(partner, pageable)
                .map(this::toDeliveryLogResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<DeliveryLogResponse> listDeliveryLogsByDateRange(Instant from, Instant to, Pageable pageable) {
        if (from.isAfter(to)) {
            throw new BadRequestException("from must be before to");
        }
        return PagedResponse.from(deliveryLogRepository.findAllByDeliveryTimeBetweenAndDeletedFalse(from, to, pageable)
                .map(this::toDeliveryLogResponse));
    }

    private VisitorPassEntity buildPassFromCreateRequest(
            VisitorPassCreateRequest request,
            UUID tenantId,
            VisitorPassStatus status,
            UUID approvedBy) {
        VisitorPassEntity pass = new VisitorPassEntity();
        pass.setTenantId(tenantId);
        pass.setPassNumber(generatePassNumber());
        pass.setVisitorId(request.visitorId());
        pass.setUnitId(request.unitId());
        pass.setVisitorName(resolveVisitorName(request.visitorName(), request.visitorId()));
        pass.setVehicleNumber(resolveVehicleNumber(request.vehicleNumber(), request.visitorId()));
        pass.setValidFrom(request.validFrom());
        pass.setValidTo(request.validTo());
        pass.setVisitDate(resolveVisitDate(request.visitDate(), request.validFrom()));
        pass.setQrCode("VIS-" + UUID.randomUUID());
        pass.setPurpose(request.purpose());
        pass.setNumberOfPersons(resolveNumberOfPersons(request.numberOfPersons()));
        pass.setApprovedBy(approvedBy);
        pass.setStatus(status);
        return pass;
    }

    private VisitorPassResponse updatePassStatus(UUID id, VisitorPassStatus status, String action, UUID actedBy) {
        VisitorPassEntity pass = getPassEntity(id);
        pass.setStatus(status);
        if (status == VisitorPassStatus.APPROVED || status == VisitorPassStatus.ACTIVE) {
            pass.setApprovedBy(actedBy);
        }
        VisitorPassEntity saved = visitorPassRepository.save(pass);
        auditLogService.logEvent(saved.getTenantId(), actedBy, action, "visitor_pass", saved.getId(), null);
        return toPassResponse(saved);
    }

    private VisitorPassEntity getPassEntity(UUID id) {
        return visitorPassRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor pass not found: " + id));
    }

    private VisitorEntity getVisitorEntity(UUID id) {
        return visitorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visitor not found: " + id));
    }

    private DomesticHelpEntity getDomesticHelpEntity(UUID id) {
        return domesticHelpRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Domestic help not found: " + id));
    }

    private BlacklistEntity getBlacklistEntity(UUID id) {
        return blacklistRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blacklist entry not found: " + id));
    }

    private String resolveVisitorName(String visitorName, UUID visitorId) {
        if (visitorName != null && !visitorName.isBlank()) {
            return visitorName;
        }
        if (visitorId == null) {
            throw new BadRequestException("visitorName is required when visitorId is not provided");
        }
        return getVisitorEntity(visitorId).getVisitorName();
    }

    private String resolveVehicleNumber(String vehicleNumber, UUID visitorId) {
        if (vehicleNumber != null && !vehicleNumber.isBlank()) {
            return vehicleNumber;
        }
        if (visitorId == null) {
            return null;
        }
        return getVisitorEntity(visitorId).getVehicleNumber();
    }

    private LocalDate resolveVisitDate(LocalDate visitDate, Instant validFrom) {
        if (visitDate != null) {
            return visitDate;
        }
        return validFrom.atZone(ZoneOffset.UTC).toLocalDate();
    }

    private int resolveNumberOfPersons(Integer value) {
        if (value == null || value < 1) {
            return 1;
        }
        return value;
    }

    private String generatePassNumber() {
        for (int i = 0; i < 10; i++) {
            String candidate = "VP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            if (!visitorPassRepository.existsByPassNumber(candidate)) {
                return candidate;
            }
        }
        return "VP-" + Instant.now().toEpochMilli();
    }

    private VisitorResponse toVisitorResponse(VisitorEntity entity) {
        return new VisitorResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getVisitorName(),
                entity.getPhone(),
                entity.getVehicleNumber(),
                entity.getVisitorType(),
                entity.getIdProofType(),
                entity.getIdProofNumber(),
                entity.getPhotoUrl());
    }

    private VisitorPassResponse toPassResponse(VisitorPassEntity pass) {
        return new VisitorPassResponse(
                pass.getId(),
                pass.getTenantId(),
                pass.getPassNumber(),
                pass.getVisitorId(),
                pass.getUnitId(),
                pass.getVisitorName(),
                pass.getVehicleNumber(),
                pass.getVisitDate(),
                pass.getValidFrom(),
                pass.getValidTo(),
                pass.getQrCode(),
                pass.getPurpose(),
                pass.getNumberOfPersons(),
                pass.getApprovedBy(),
                pass.getStatus());
    }

    private VisitorLogResponse toVisitorLogResponse(VisitorEntryExitLogEntity entity) {
        return new VisitorLogResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getVisitorPassId(),
                entity.getEntryTime(),
                entity.getExitTime(),
                entity.getEntryGate(),
                entity.getExitGate(),
                entity.getSecurityGuardEntry(),
                entity.getSecurityGuardExit(),
                entity.getFaceCaptureUrl());
    }

    private DomesticHelpResponse toDomesticHelpResponse(DomesticHelpEntity entity) {
        return new DomesticHelpResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getHelpName(),
                entity.getPhone(),
                entity.getHelpType(),
                entity.isPermanentPass(),
                entity.isPoliceVerificationDone(),
                entity.getVerificationDate(),
                entity.getPhotoUrl(),
                entity.getRegisteredBy());
    }

    private DomesticHelpUnitMappingResponse toDomesticHelpUnitMappingResponse(DomesticHelpUnitMappingEntity entity) {
        return new DomesticHelpUnitMappingResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getDomesticHelpId(),
                entity.getUnitId(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.isActive());
    }

    private BlacklistResponse toBlacklistResponse(BlacklistEntity entity) {
        return new BlacklistResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getPersonName(),
                entity.getPhone(),
                entity.getReason(),
                entity.getBlacklistedBy(),
                entity.getBlacklistDate(),
                entity.isActive());
    }

    private DeliveryLogResponse toDeliveryLogResponse(DeliveryLogEntity entity) {
        return new DeliveryLogResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getUnitId(),
                entity.getDeliveryPartner(),
                entity.getTrackingNumber(),
                entity.getDeliveryTime(),
                entity.getReceivedBy(),
                entity.getSecurityGuardId(),
                entity.getPhotoUrl());
    }
}
