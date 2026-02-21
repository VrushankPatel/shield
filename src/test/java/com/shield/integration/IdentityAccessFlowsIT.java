package com.shield.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import com.shield.integration.support.IntegrationTestBase;
import com.shield.module.auth.entity.AuthTokenEntity;
import com.shield.module.auth.entity.AuthTokenType;
import com.shield.module.auth.repository.AuthTokenRepository;
import com.shield.module.notification.service.LoggingSmsOtpSender;
import com.shield.module.tenant.entity.TenantEntity;
import com.shield.module.tenant.repository.TenantRepository;
import com.shield.module.unit.entity.UnitEntity;
import com.shield.module.unit.entity.UnitStatus;
import com.shield.module.unit.repository.UnitRepository;
import com.shield.module.user.entity.UserEntity;
import com.shield.module.user.entity.UserRole;
import com.shield.module.user.entity.UserStatus;
import com.shield.module.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

class IdentityAccessFlowsIT extends IntegrationTestBase {

    private static final String ADMIN_PASSWORD = "password123";

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoggingSmsOtpSender loggingSmsOtpSender;

    @Test
    @SuppressWarnings("java:S5961")
    void identityLifecycleShouldSupportRegistrationVerificationAndResidentOperations() {
        TenantEntity tenantOne = createTenant("Identity Society One");
        UnitEntity unitOne = createUnit(tenantOne.getId(), "A-101", UnitStatus.ACTIVE);
        UnitEntity vacantUnit = createUnit(tenantOne.getId(), "A-102", UnitStatus.VACANT);

        UserEntity admin = createUser(tenantOne.getId(), unitOne.getId(), "Admin One", "admin.identity@shield.dev", UserRole.ADMIN, ADMIN_PASSWORD);
        UserEntity security = createUser(tenantOne.getId(), unitOne.getId(), "Security One", "security.identity@shield.dev", UserRole.SECURITY, ADMIN_PASSWORD);

        TenantEntity tenantTwo = createTenant("Identity Society Two");
        UnitEntity unitTwo = createUnit(tenantTwo.getId(), "B-201", UnitStatus.ACTIVE);
        UserEntity outsider = createUser(tenantTwo.getId(), unitTwo.getId(), "Outsider", "outsider.identity@shield.dev", UserRole.TENANT, ADMIN_PASSWORD);

        String adminToken = login(admin.getEmail(), ADMIN_PASSWORD);
        String securityToken = login(security.getEmail(), ADMIN_PASSWORD);

        String residentEmail = "resident.identity@shield.dev";
        String residentInitialPassword = "Resident#123";

        String residentId = given()
                .contentType("application/json")
                .body(Map.of(
                        "tenantId", tenantOne.getId(),
                        "unitId", unitOne.getId(),
                        "name", "Resident One",
                        "email", residentEmail,
                        "phone", "9999999998",
                        "password", residentInitialPassword,
                        "role", "TENANT"))
                .when()
                .post("/auth/register")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.emailVerificationRequired", equalTo(true))
                .extract()
                .path("data.userId");

        given()
                .contentType("application/json")
                .body(Map.of("email", residentEmail, "password", residentInitialPassword))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        UUID residentUuid = UUID.fromString(residentId);
        AuthTokenEntity verificationToken = authTokenRepository
                .findAllByTenantIdAndUserIdAndTokenTypeAndConsumedAtIsNullAndDeletedFalse(
                        tenantOne.getId(),
                        residentUuid,
                        AuthTokenType.EMAIL_VERIFICATION)
                .stream()
                .findFirst()
                .orElseThrow();

        given()
                .when()
                .get("/auth/verify-email/{token}", verificationToken.getTokenValue())
                .then()
                .statusCode(HttpStatus.OK.value());

        String otpChallenge = given()
                .contentType("application/json")
                .body(Map.of("email", residentEmail))
                .when()
                .post("/auth/login/otp/send")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.challengeToken", notNullValue())
                .extract()
                .path("data.challengeToken");

        String latestOtpCode = loggingSmsOtpSender.getLastOtp("9999999998").orElseThrow();

        given()
                .contentType("application/json")
                .body(Map.of("challengeToken", otpChallenge, "otpCode", "000000"))
                .when()
                .post("/auth/login/otp/verify")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        String residentToken = given()
                .contentType("application/json")
                .body(Map.of("challengeToken", otpChallenge, "otpCode", latestOtpCode))
                .when()
                .post("/auth/login/otp/verify")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.accessToken", notNullValue())
                .extract()
                .path("data.accessToken");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of(
                        "currentPassword", "Wrong#123",
                        "newPassword", "Resident#456"))
                .when()
                .post("/auth/change-password")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of(
                        "currentPassword", residentInitialPassword,
                        "newPassword", "Resident#456"))
                .when()
                .post("/auth/change-password")
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .contentType("application/json")
                .body(Map.of("email", residentEmail, "password", residentInitialPassword))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        residentToken = login(residentEmail, "Resident#456");

        given()
                .contentType("application/json")
                .body(Map.of("email", residentEmail))
                .when()
                .post("/auth/forgot-password")
                .then()
                .statusCode(HttpStatus.OK.value());

        AuthTokenEntity resetToken = authTokenRepository
                .findAllByTenantIdAndUserIdAndTokenTypeAndConsumedAtIsNullAndDeletedFalse(
                        tenantOne.getId(),
                        residentUuid,
                        AuthTokenType.PASSWORD_RESET)
                .stream()
                .findFirst()
                .orElseThrow();

        given()
                .contentType("application/json")
                .body(Map.of(
                        "token", resetToken.getTokenValue(),
                        "newPassword", "Resident#789"))
                .when()
                .post("/auth/reset-password")
                .then()
                .statusCode(HttpStatus.OK.value());

        residentToken = login(residentEmail, "Resident#789");

        String permissionId = given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/permissions")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(1))
                .extract()
                .path("data.content[0].id");

        String customRoleId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "code", "HELPDESK_AGENT",
                        "name", "Helpdesk Agent",
                        "description", "Handles resident escalations",
                        "systemRole", false))
                .when()
                .post("/roles")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.code", equalTo("HELPDESK_AGENT"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("permissionIds", List.of(UUID.fromString(permissionId))))
                .when()
                .post("/roles/{id}/permissions", customRoleId)
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("roleId", UUID.fromString(customRoleId)))
                .when()
                .post("/users/{id}/roles", residentUuid)
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/users/{id}/permissions", residentUuid)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.roles", hasItem("TENANT"))
                .body("data.roles", hasItem("HELPDESK_AGENT"));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/users/unit/{unitId}", unitOne.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(3));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/users/role/{role}", "TENANT")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.email", hasItem(residentEmail));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/users/export")
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "users", List.of(
                                Map.of(
                                        "unitId", unitOne.getId(),
                                        "name", "Imported Resident",
                                        "email", "imported.identity@shield.dev",
                                        "phone", "9999999997",
                                        "password", "Imported#123",
                                        "role", "TENANT"))))
                .when()
                .post("/users/bulk-import")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.createdCount", equalTo(1));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/units/block/{block}", "A")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(2));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/units/available")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.unitNumber", hasItem(vacantUnit.getUnitNumber()));

        String kycId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of(
                        "userId", residentUuid,
                        "documentType", "PAN",
                        "documentNumber", "ABCDE1234F",
                        "documentUrl", "https://files.example/pan.pdf"))
                .when()
                .post("/kyc/upload")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.verificationStatus", equalTo("PENDING"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/kyc/{id}/verify", kycId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.verificationStatus", equalTo("VERIFIED"))
                .body("data.verifiedBy", equalTo(admin.getId().toString()));

        String moveRecordId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + residentToken)
                .body(Map.of(
                        "unitId", unitOne.getId(),
                        "userId", residentUuid,
                        "effectiveDate", LocalDate.now().plusDays(3).toString(),
                        "securityDeposit", 25000,
                        "agreementUrl", "https://files.example/agreement.pdf"))
                .when()
                .post("/move-records/move-in")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("PENDING"))
                .body("data.moveType", equalTo("MOVE_IN"))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("decisionNotes", "Move-in approved by committee"))
                .when()
                .post("/move-records/{id}/approve", moveRecordId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("APPROVED"))
                .body("data.approvedBy", equalTo(admin.getId().toString()));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/units/{id}/members", unitOne.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.email", hasItem(residentEmail));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/units/{id}/history", unitOne.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.content.size()", greaterThanOrEqualTo(1));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "ownershipStatus", "RENTED",
                        "notes", "Marked as rented"))
                .when()
                .patch("/units/{id}/ownership", unitOne.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.previousOwnershipStatus", equalTo("OWNED"))
                .body("data.ownershipStatus", equalTo("RENTED"));

        String parkingSlotId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "slotNumber", "P-101",
                        "parkingType", "COVERED",
                        "vehicleType", "FOUR_WHEELER"))
                .when()
                .post("/parking-slots")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.allocated", equalTo(false))
                .extract()
                .path("data.id");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("unitId", unitOne.getId()))
                .when()
                .post("/parking-slots/{id}/allocate", parkingSlotId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.allocated", equalTo(true))
                .body("data.unitId", equalTo(unitOne.getId().toString()));

        String digitalIdCardId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of(
                        "userId", residentUuid,
                        "validityDays", 365,
                        "qrCodeUrl", "https://files.example/qr/resident-one.png"))
                .when()
                .post("/digital-id-cards/generate")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.qrCodeData", startsWith("SID-"))
                .body("data.active", equalTo(true))
                .extract()
                .path("data.id");

        String qrCodeData = given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/digital-id-cards/{id}", digitalIdCardId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.userId", equalTo(residentUuid.toString()))
                .extract()
                .path("data.qrCodeData");

        given()
                .header("Authorization", "Bearer " + securityToken)
                .when()
                .get("/digital-id-cards/verify/{qrCode}", qrCodeData)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.valid", equalTo(true))
                .body("data.message", equalTo("Card is valid"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/digital-id-cards/{id}/deactivate", digitalIdCardId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.active", equalTo(false))
                .body("data.deactivatedAt", notNullValue());

        given()
                .header("Authorization", "Bearer " + securityToken)
                .when()
                .get("/digital-id-cards/verify/{qrCode}", qrCodeData)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.valid", equalTo(false));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/users/{id}/roles/{roleId}", residentUuid, customRoleId)
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/roles/{id}", customRoleId)
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("status", "INACTIVE"))
                .when()
                .patch("/users/{id}/status", residentUuid)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("INACTIVE"));

        given()
                .contentType("application/json")
                .body(Map.of("email", residentEmail, "password", "Resident#789"))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(Map.of("status", "ACTIVE"))
                .when()
                .patch("/users/{id}/status", residentUuid)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.status", equalTo("ACTIVE"));

        String outsiderToken = login(outsider.getEmail(), ADMIN_PASSWORD);

        given()
                .header("Authorization", "Bearer " + outsiderToken)
                .when()
                .get("/users/{id}/permissions", residentUuid)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());

        given()
                .header("Authorization", "Bearer " + outsiderToken)
                .when()
                .get("/move-records/{id}", moveRecordId)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private TenantEntity createTenant(String name) {
        TenantEntity entity = new TenantEntity();
        entity.setName(name);
        entity.setAddress("Integration Address");
        return tenantRepository.save(entity);
    }

    private UnitEntity createUnit(UUID tenantId, String unitNumber, UnitStatus status) {
        UnitEntity unit = new UnitEntity();
        unit.setTenantId(tenantId);
        unit.setUnitNumber(unitNumber);
        unit.setBlock("A");
        unit.setType("FLAT");
        unit.setSquareFeet(BigDecimal.valueOf(1000));
        unit.setStatus(status);
        return unitRepository.save(unit);
    }

    private UserEntity createUser(UUID tenantId, UUID unitId, String name, String email, UserRole role, String password) {
        UserEntity user = new UserEntity();
        user.setTenantId(tenantId);
        user.setUnitId(unitId);
        user.setName(name);
        user.setEmail(email);
        user.setPhone("9999999999");
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private String login(String email, String password) {
        return given()
                .contentType("application/json")
                .body(Map.of("email", email, "password", password))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("data.accessToken");
    }
}
