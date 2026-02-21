package com.shield.module.platform.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.UnauthorizedException;
import com.shield.module.platform.dto.RootAuthResponse;
import com.shield.module.platform.dto.RootChangePasswordRequest;
import com.shield.module.platform.dto.RootLoginRequest;
import com.shield.module.platform.dto.RootRefreshRequest;
import com.shield.module.platform.dto.SocietyOnboardingRequest;
import com.shield.module.platform.dto.SocietyOnboardingResponse;
import com.shield.module.platform.entity.PlatformRootAccountEntity;
import com.shield.module.platform.repository.PlatformRootAccountRepository;
import com.shield.module.platform.verification.RootContactVerificationService;
import com.shield.module.tenant.entity.TenantEntity;
import com.shield.module.tenant.repository.TenantRepository;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.entity.UserRole;
import com.shield.module.user.entity.UserStatus;
import com.shield.module.user.repository.UserRepository;
import com.shield.security.jwt.JwtService;
import com.shield.security.model.ShieldPrincipal;
import io.jsonwebtoken.Claims;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlatformRootService {

    public static final String ROOT_LOGIN_ID = "root";
    private static final String ROOT_PRINCIPAL_TYPE = "ROOT";
    private static final String ROOT_ROLE = "ROOT";
    private static final String ENTITY_PLATFORM_ROOT_ACCOUNT = "platform_root_account";

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}";
    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;
    private static final int GENERATED_PASSWORD_LENGTH = 32;

    private final PlatformRootAccountRepository platformRootAccountRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    @Lazy
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final RootContactVerificationService rootContactVerificationService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${shield.security.jwt.access-token-ttl-minutes}")
    private long accessTokenTtlMinutes;

    @Value("${shield.platform.root.lockout.max-failed-attempts:5}")
    private int maxFailedLoginAttempts;

    @Value("${shield.platform.root.lockout.duration-minutes:30}")
    private long lockoutDurationMinutes;

    @Transactional
    public Optional<String> ensureRootAccountAndGeneratePasswordIfMissing() {
        PlatformRootAccountEntity rootAccount = platformRootAccountRepository.findByLoginIdAndDeletedFalse(ROOT_LOGIN_ID)
                .orElseGet(this::createInitialRootAccount);

        if (StringUtils.hasText(rootAccount.getPasswordHash())) {
            return Optional.empty();
        }

        String generatedPassword = generateSecurePassword();
        rootAccount.setPasswordHash(passwordEncoder.encode(generatedPassword));
        rootAccount.setPasswordChangeRequired(true);
        rootAccount.setEmailVerified(true);
        rootAccount.setMobileVerified(true);
        rootAccount.setActive(true);
        rootAccount.setTokenVersion(safeTokenVersion(rootAccount));
        resetRootLoginFailureState(rootAccount);
        rootAccount.setLastLoginAt(null);
        platformRootAccountRepository.save(rootAccount);

        auditLogService.logEvent(null, rootAccount.getId(), "ROOT_PASSWORD_GENERATED", ENTITY_PLATFORM_ROOT_ACCOUNT, rootAccount.getId(), null);
        return Optional.of(generatedPassword);
    }

    @Transactional(readOnly = true)
    public boolean isRootTokenVersionValid(UUID rootAccountId, Long tokenVersion) {
        if (rootAccountId == null || tokenVersion == null) {
            return false;
        }

        return platformRootAccountRepository.findByIdAndDeletedFalse(rootAccountId)
                .filter(PlatformRootAccountEntity::isActive)
                .map(account -> safeTokenVersion(account) == tokenVersion)
                .orElse(false);
    }

    @Transactional
    public RootAuthResponse login(RootLoginRequest request) {
        if (!ROOT_LOGIN_ID.equalsIgnoreCase(request.loginId().trim())) {
            throw new UnauthorizedException("Invalid root credentials");
        }

        PlatformRootAccountEntity rootAccount = platformRootAccountRepository.findByLoginIdAndDeletedFalse(ROOT_LOGIN_ID)
                .orElseThrow(() -> new UnauthorizedException("Root account is not initialized"));

        if (!rootAccount.isActive() || !StringUtils.hasText(rootAccount.getPasswordHash())) {
            throw new UnauthorizedException("Root account is not active");
        }

        if (isLocked(rootAccount)) {
            auditLogService.logEvent(
                    null,
                    rootAccount.getId(),
                    "ROOT_LOGIN_BLOCKED",
                    ENTITY_PLATFORM_ROOT_ACCOUNT,
                    rootAccount.getId(),
                    "reason=lockout,lockedUntil=" + rootAccount.getLockedUntil());
            throw new UnauthorizedException("Root account is temporarily locked due to failed login attempts. Try again later.");
        }

        if (!passwordEncoder.matches(request.password(), rootAccount.getPasswordHash())) {
            onRootLoginFailed(rootAccount);
            throw new UnauthorizedException("Invalid root credentials");
        }

        resetRootLoginFailureState(rootAccount);
        rootAccount.setLastLoginAt(Instant.now());
        platformRootAccountRepository.save(rootAccount);

        long tokenVersion = safeTokenVersion(rootAccount);
        String accessToken = jwtService.generateRootAccessToken(rootAccount.getId(), rootAccount.getLoginId(), tokenVersion);
        String refreshToken = jwtService.generateRootRefreshToken(rootAccount.getId(), rootAccount.getLoginId(), tokenVersion);

        auditLogService.logEvent(
                null,
                rootAccount.getId(),
                "ROOT_LOGIN",
                ENTITY_PLATFORM_ROOT_ACCOUNT,
                rootAccount.getId(),
                "passwordChangeRequired=" + rootAccount.isPasswordChangeRequired());
        return new RootAuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                accessTokenTtlMinutes * 60,
                rootAccount.isPasswordChangeRequired());
    }

    public RootAuthResponse refresh(RootRefreshRequest request) {
        String refreshToken = jwtService.stripBearerPrefix(request.refreshToken());
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        Claims claims = jwtService.parseClaims(refreshToken);
        String tokenType = claims.get("tokenType", String.class);
        String principalType = claims.get("principalType", String.class);
        if (!"refresh".equals(tokenType) || !ROOT_PRINCIPAL_TYPE.equalsIgnoreCase(principalType)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        UUID rootAccountId = parseUuidClaim(claims, "userId");
        long tokenVersion = parseLongClaim(claims, "tokenVersion");

        PlatformRootAccountEntity rootAccount = platformRootAccountRepository.findByIdAndDeletedFalse(rootAccountId)
                .filter(PlatformRootAccountEntity::isActive)
                .orElseThrow(() -> new UnauthorizedException("Root account not found"));

        if (safeTokenVersion(rootAccount) != tokenVersion) {
            throw new UnauthorizedException("Refresh token is no longer valid");
        }

        String accessToken = jwtService.generateRootAccessToken(rootAccount.getId(), rootAccount.getLoginId(), tokenVersion);
        String rotatedRefreshToken = jwtService.generateRootRefreshToken(rootAccount.getId(), rootAccount.getLoginId(), tokenVersion);

        return new RootAuthResponse(
                accessToken,
                rotatedRefreshToken,
                "Bearer",
                accessTokenTtlMinutes * 60,
                rootAccount.isPasswordChangeRequired());
    }

    @Transactional
    public void changePassword(ShieldPrincipal principal, RootChangePasswordRequest request) {
        PlatformRootAccountEntity rootAccount = requireRootAccount(principal);

        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        if (StringUtils.hasText(rootAccount.getPasswordHash())
                && passwordEncoder.matches(request.newPassword(), rootAccount.getPasswordHash())) {
            throw new BadRequestException("New password must be different from current password");
        }

        boolean emailVerified = rootContactVerificationService.verifyEmailOwnership(request.email());
        boolean mobileVerified = rootContactVerificationService.verifyMobileOwnership(request.mobile());

        if (!emailVerified || !mobileVerified) {
            throw new BadRequestException("Email or mobile verification failed");
        }

        rootAccount.setEmail(request.email().trim().toLowerCase());
        rootAccount.setMobile(request.mobile().trim());
        rootAccount.setEmailVerified(emailVerified);
        rootAccount.setMobileVerified(mobileVerified);
        rootAccount.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        rootAccount.setPasswordChangeRequired(false);
        rootAccount.setTokenVersion(safeTokenVersion(rootAccount) + 1L);
        resetRootLoginFailureState(rootAccount);
        platformRootAccountRepository.save(rootAccount);

        auditLogService.logEvent(
                null,
                rootAccount.getId(),
                "ROOT_PASSWORD_CHANGED",
                ENTITY_PLATFORM_ROOT_ACCOUNT,
                rootAccount.getId(),
                "emailVerified=" + emailVerified
                        + ",mobileVerified=" + mobileVerified
                        + ",emailProvider=" + rootContactVerificationService.emailProvider()
                        + ",mobileProvider=" + rootContactVerificationService.mobileProvider());
    }

    @Transactional
    public SocietyOnboardingResponse createSocietyWithAdmin(ShieldPrincipal principal, SocietyOnboardingRequest request) {
        PlatformRootAccountEntity rootAccount = requireRootAccount(principal);
        if (rootAccount.isPasswordChangeRequired()) {
            throw new BadRequestException("Root password change is required before onboarding societies");
        }

        auditLogService.logEvent(
                null,
                rootAccount.getId(),
                "ROOT_ONBOARDING_STARTED",
                ENTITY_PLATFORM_ROOT_ACCOUNT,
                rootAccount.getId(),
                "societyName=" + request.societyName().trim()
                        + ",adminEmail=" + request.adminEmail().trim().toLowerCase());

        TenantEntity tenant = new TenantEntity();
        tenant.setName(request.societyName().trim());
        tenant.setAddress(request.societyAddress());
        TenantEntity savedTenant = tenantRepository.save(tenant);

        UserEntity admin = new UserEntity();
        admin.setTenantId(savedTenant.getId());
        admin.setName(request.adminName().trim());
        admin.setEmail(request.adminEmail().trim().toLowerCase());
        admin.setPhone(request.adminPhone());
        admin.setPasswordHash(passwordEncoder.encode(request.adminPassword()));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);

        UserEntity savedAdmin = userRepository.save(admin);

        auditLogService.logEvent(
                null,
                rootAccount.getId(),
                "ROOT_SOCIETY_CREATED",
                "tenant",
                savedTenant.getId(),
                "societyName=" + savedTenant.getName());
        auditLogService.logEvent(
                savedTenant.getId(),
                rootAccount.getId(),
                "ROOT_ADMIN_CREATED",
                "users",
                savedAdmin.getId(),
                "adminEmail=" + savedAdmin.getEmail());
        auditLogService.logEvent(
                savedTenant.getId(),
                rootAccount.getId(),
                "ROOT_ONBOARDING_COMPLETED",
                "tenant",
                savedTenant.getId(),
                "adminUserId=" + savedAdmin.getId());

        return new SocietyOnboardingResponse(savedTenant.getId(), savedAdmin.getId(), savedAdmin.getEmail());
    }

    private PlatformRootAccountEntity requireRootAccount(ShieldPrincipal principal) {
        if (principal == null
                || !ROOT_ROLE.equalsIgnoreCase(principal.role())
                || !ROOT_PRINCIPAL_TYPE.equalsIgnoreCase(principal.principalType())) {
            throw new UnauthorizedException("Root privileges are required");
        }

        PlatformRootAccountEntity rootAccount = platformRootAccountRepository.findByIdAndDeletedFalse(principal.userId())
                .orElseThrow(() -> new UnauthorizedException("Root account not found"));

        if (!rootAccount.isActive()) {
            throw new UnauthorizedException("Root account is inactive");
        }

        if (safeTokenVersion(rootAccount) != principal.tokenVersion()) {
            throw new UnauthorizedException("Root session is no longer valid");
        }

        return rootAccount;
    }

    private void onRootLoginFailed(PlatformRootAccountEntity rootAccount) {
        int currentAttempts = rootAccount.getFailedLoginAttempts() == null ? 0 : rootAccount.getFailedLoginAttempts();
        int updatedAttempts = currentAttempts + 1;
        rootAccount.setFailedLoginAttempts(updatedAttempts);

        String payload = "failedAttempts=" + updatedAttempts;
        if (updatedAttempts >= maxFailedLoginAttempts) {
            Instant lockedUntil = Instant.now().plus(lockoutDurationMinutes, ChronoUnit.MINUTES);
            rootAccount.setLockedUntil(lockedUntil);
            rootAccount.setFailedLoginAttempts(0);
            payload = payload + ",lockoutMinutes=" + lockoutDurationMinutes + ",lockedUntil=" + lockedUntil;
        }

        platformRootAccountRepository.save(rootAccount);
        auditLogService.logEvent(
                null,
                rootAccount.getId(),
                "ROOT_LOGIN_FAILED",
                ENTITY_PLATFORM_ROOT_ACCOUNT,
                rootAccount.getId(),
                payload);
    }

    private void resetRootLoginFailureState(PlatformRootAccountEntity rootAccount) {
        rootAccount.setFailedLoginAttempts(0);
        rootAccount.setLockedUntil(null);
    }

    private boolean isLocked(PlatformRootAccountEntity rootAccount) {
        Instant lockedUntil = rootAccount.getLockedUntil();
        return lockedUntil != null && lockedUntil.isAfter(Instant.now());
    }

    private PlatformRootAccountEntity createInitialRootAccount() {
        PlatformRootAccountEntity rootAccount = new PlatformRootAccountEntity();
        rootAccount.setLoginId(ROOT_LOGIN_ID);
        rootAccount.setEmailVerified(true);
        rootAccount.setMobileVerified(true);
        rootAccount.setPasswordChangeRequired(true);
        rootAccount.setTokenVersion(0L);
        rootAccount.setFailedLoginAttempts(0);
        rootAccount.setLockedUntil(null);
        rootAccount.setActive(true);
        return platformRootAccountRepository.save(rootAccount);
    }

    private String generateSecurePassword() {
        char[] password = new char[GENERATED_PASSWORD_LENGTH];
        password[0] = randomChar(UPPER);
        password[1] = randomChar(LOWER);
        password[2] = randomChar(DIGITS);
        password[3] = randomChar(SPECIAL);

        for (int i = 4; i < password.length; i++) {
            password[i] = randomChar(ALL);
        }

        shuffle(password);
        return new String(password);
    }

    private void shuffle(char[] values) {
        for (int i = values.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temp = values[i];
            values[i] = values[j];
            values[j] = temp;
        }
    }

    private char randomChar(String source) {
        return source.charAt(secureRandom.nextInt(source.length()));
    }

    private long safeTokenVersion(PlatformRootAccountEntity rootAccount) {
        return rootAccount.getTokenVersion() == null ? 0L : rootAccount.getTokenVersion();
    }

    private UUID parseUuidClaim(Claims claims, String key) {
        String raw = claims.get(key, String.class);
        if (!StringUtils.hasText(raw)) {
            throw new UnauthorizedException("Missing token claim: " + key);
        }
        return UUID.fromString(raw);
    }

    private long parseLongClaim(Claims claims, String key) {
        try {
            Object value = claims.get(key);
            if (value instanceof Number number) {
                return number.longValue();
            }
            if (value instanceof String stringValue && StringUtils.hasText(stringValue)) {
                return Long.parseLong(stringValue);
            }
            return 0L;
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid token claim: " + key);
        }
    }
}
