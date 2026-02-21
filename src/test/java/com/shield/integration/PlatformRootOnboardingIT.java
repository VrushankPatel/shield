package com.shield.integration;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.shield.integration.support.IntegrationTestBase;
import com.shield.module.platform.entity.PlatformRootAccountEntity;
import com.shield.module.platform.repository.PlatformRootAccountRepository;
import com.shield.module.tenant.repository.TenantRepository;
import com.shield.module.user.entity.UserRole;
import com.shield.module.user.entity.UserStatus;
import com.shield.module.user.repository.UserRepository;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

class PlatformRootOnboardingIT extends IntegrationTestBase {

    private static final String ROOT_LOGIN_ID = "root";
    private static final String ROOT_INITIAL_PASSWORD = "RootInitial#123";
    private static final String ROOT_NEW_PASSWORD = "RootNewPassword#123";

    @Autowired
    private PlatformRootAccountRepository platformRootAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void rootLifecycleShouldRequirePasswordChangeAndSupportSocietyOnboarding() {
        PlatformRootAccountEntity rootAccount = platformRootAccountRepository.findByLoginIdAndDeletedFalse(ROOT_LOGIN_ID)
                .orElseGet(() -> {
                    PlatformRootAccountEntity entity = new PlatformRootAccountEntity();
                    entity.setLoginId(ROOT_LOGIN_ID);
                    return entity;
                });
        rootAccount.setPasswordHash(passwordEncoder.encode(ROOT_INITIAL_PASSWORD));
        rootAccount.setPasswordChangeRequired(true);
        rootAccount.setTokenVersion(0L);
        rootAccount.setEmail(null);
        rootAccount.setMobile(null);
        rootAccount.setEmailVerified(true);
        rootAccount.setMobileVerified(true);
        rootAccount.setActive(true);
        platformRootAccountRepository.save(rootAccount);

        String initialAccessToken = given()
                .contentType("application/json")
                .body(Map.of(
                        "loginId", ROOT_LOGIN_ID,
                        "password", ROOT_INITIAL_PASSWORD))
                .when()
                .post("/platform/root/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.accessToken", notNullValue())
                .body("data.refreshToken", notNullValue())
                .body("data.passwordChangeRequired", equalTo(true))
                .extract()
                .path("data.accessToken");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + initialAccessToken)
                .body(Map.of(
                        "societyName", "Sunshine Heights",
                        "societyAddress", "Ahmedabad",
                        "adminName", "Society Admin",
                        "adminEmail", "admin@sunshine.dev",
                        "adminPhone", "9999999998",
                        "adminPassword", "AdminStrong#123"))
                .when()
                .post("/platform/societies")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", equalTo("Root password change is required before onboarding societies"));

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + initialAccessToken)
                .body(Map.of(
                        "email", "root@shield.dev",
                        "mobile", "9999999999",
                        "newPassword", ROOT_NEW_PASSWORD,
                        "confirmNewPassword", ROOT_NEW_PASSWORD))
                .when()
                .post("/platform/root/change-password")
                .then()
                .statusCode(HttpStatus.OK.value());

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + initialAccessToken)
                .body(Map.of(
                        "societyName", "Sunshine Heights",
                        "societyAddress", "Ahmedabad",
                        "adminName", "Society Admin",
                        "adminEmail", "admin@sunshine.dev",
                        "adminPhone", "9999999998",
                        "adminPassword", "AdminStrong#123"))
                .when()
                .post("/platform/societies")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        String newAccessToken = given()
                .contentType("application/json")
                .body(Map.of(
                        "loginId", ROOT_LOGIN_ID,
                        "password", ROOT_NEW_PASSWORD))
                .when()
                .post("/platform/root/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.passwordChangeRequired", equalTo(false))
                .extract()
                .path("data.accessToken");

        String tenantId = given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + newAccessToken)
                .body(Map.of(
                        "societyName", "Sunshine Heights",
                        "societyAddress", "Ahmedabad",
                        "adminName", "Society Admin",
                        "adminEmail", "admin@sunshine.dev",
                        "adminPhone", "9999999998",
                        "adminPassword", "AdminStrong#123"))
                .when()
                .post("/platform/societies")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.societyId", notNullValue())
                .body("data.adminUserId", notNullValue())
                .extract()
                .path("data.societyId");

        UUID tenantUuid = UUID.fromString(tenantId);
        assertTrue(tenantRepository.findById(tenantUuid).isPresent());

        var createdAdmin = userRepository.findByTenantIdAndEmailIgnoreCaseAndDeletedFalse(tenantUuid, "admin@sunshine.dev")
                .orElseThrow();
        assertEquals(UserRole.ADMIN, createdAdmin.getRole());
        assertEquals(UserStatus.ACTIVE, createdAdmin.getStatus());
    }
}
