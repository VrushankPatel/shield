package com.shield.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import com.shield.integration.support.IntegrationTestBase;
import com.shield.module.auth.entity.AuthTokenEntity;
import com.shield.module.auth.entity.AuthTokenType;
import com.shield.module.auth.repository.AuthTokenRepository;
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
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthSessionSecurityIT extends IntegrationTestBase {

    private static final String PASSWORD_V1 = "Session#Password1";
    private static final String PASSWORD_V2 = "Session#Password2";
    private static final String PASSWORD_V3 = "Session#Password3";

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

    @Test
    void refreshRotationLogoutAndPasswordEventsShouldInvalidateSessions() {
        TenantEntity tenant = createTenant("Session Security Society");
        UnitEntity unit = createUnit(tenant.getId(), "S-101");
        UserEntity user = createUser(tenant.getId(), unit.getId(), "Session User", "session.user@shield.dev", PASSWORD_V1);

        Map<String, String> firstLogin = loginForTokens(user.getEmail(), PASSWORD_V1);
        String firstRefreshToken = firstLogin.get("refreshToken");

        Map<String, String> rotatedAfterRefresh = refreshForTokens(firstRefreshToken, "/auth/refresh");
        String secondAccessToken = rotatedAfterRefresh.get("accessToken");
        String secondRefreshToken = rotatedAfterRefresh.get("refreshToken");

        assertRefreshRejected(firstRefreshToken, "/auth/refresh");

        given()
                .header("Authorization", "Bearer " + secondAccessToken)
                .when()
                .post("/auth/logout")
                .then()
                .statusCode(HttpStatus.OK.value());

        assertRefreshRejected(secondRefreshToken, "/auth/refresh-token");

        Map<String, String> postLogoutLogin = loginForTokens(user.getEmail(), PASSWORD_V1);
        String preChangeRefreshToken = postLogoutLogin.get("refreshToken");

        given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + postLogoutLogin.get("accessToken"))
                .body(Map.of(
                        "currentPassword", PASSWORD_V1,
                        "newPassword", PASSWORD_V2))
                .when()
                .post("/auth/change-password")
                .then()
                .statusCode(HttpStatus.OK.value());

        assertRefreshRejected(preChangeRefreshToken, "/auth/refresh");

        given()
                .contentType("application/json")
                .body(Map.of("email", user.getEmail(), "password", PASSWORD_V1))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

        Map<String, String> postChangeLogin = loginForTokens(user.getEmail(), PASSWORD_V2);
        String preResetRefreshToken = postChangeLogin.get("refreshToken");

        given()
                .contentType("application/json")
                .body(Map.of("email", user.getEmail()))
                .when()
                .post("/auth/forgot-password")
                .then()
                .statusCode(HttpStatus.OK.value());

        AuthTokenEntity resetToken = authTokenRepository
                .findAllByTenantIdAndUserIdAndTokenTypeAndConsumedAtIsNullAndDeletedFalse(
                        tenant.getId(),
                        user.getId(),
                        AuthTokenType.PASSWORD_RESET)
                .stream()
                .findFirst()
                .orElseThrow();

        given()
                .contentType("application/json")
                .body(Map.of(
                        "token", resetToken.getTokenValue(),
                        "newPassword", PASSWORD_V3))
                .when()
                .post("/auth/reset-password")
                .then()
                .statusCode(HttpStatus.OK.value());

        assertRefreshRejected(preResetRefreshToken, "/auth/refresh");
        loginForTokens(user.getEmail(), PASSWORD_V3);
    }

    private Map<String, String> loginForTokens(String email, String password) {
        Map<String, String> tokens = given()
                .contentType("application/json")
                .body(Map.of("email", email, "password", password))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.accessToken", notNullValue())
                .body("data.refreshToken", notNullValue())
                .extract()
                .path("data");

        return Map.of(
                "accessToken", tokens.get("accessToken"),
                "refreshToken", tokens.get("refreshToken"));
    }

    private Map<String, String> refreshForTokens(String refreshToken, String path) {
        Map<String, String> tokens = given()
                .contentType("application/json")
                .body(Map.of("refreshToken", refreshToken))
                .when()
                .post(path)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("data.accessToken", notNullValue())
                .body("data.refreshToken", notNullValue())
                .extract()
                .path("data");

        return Map.of(
                "accessToken", tokens.get("accessToken"),
                "refreshToken", tokens.get("refreshToken"));
    }

    private void assertRefreshRejected(String refreshToken, String path) {
        given()
                .contentType("application/json")
                .body(Map.of("refreshToken", refreshToken))
                .when()
                .post(path)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    private TenantEntity createTenant(String name) {
        TenantEntity entity = new TenantEntity();
        entity.setName(name);
        entity.setAddress("Integration Address");
        return tenantRepository.save(entity);
    }

    private UnitEntity createUnit(UUID tenantId, String unitNumber) {
        UnitEntity unit = new UnitEntity();
        unit.setTenantId(tenantId);
        unit.setUnitNumber(unitNumber);
        unit.setBlock("S");
        unit.setType("FLAT");
        unit.setSquareFeet(BigDecimal.valueOf(1000));
        unit.setStatus(UnitStatus.ACTIVE);
        return unitRepository.save(unit);
    }

    private UserEntity createUser(UUID tenantId, UUID unitId, String name, String email, String password) {
        UserEntity user = new UserEntity();
        user.setTenantId(tenantId);
        user.setUnitId(unitId);
        user.setName(name);
        user.setEmail(email);
        user.setPhone("9999999999");
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.TENANT);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }
}
