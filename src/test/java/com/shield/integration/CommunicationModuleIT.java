package com.shield.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.shield.integration.support.IntegrationTestBase;
import com.shield.module.announcement.dto.AnnouncementAttachmentRequest;
import com.shield.module.announcement.dto.AnnouncementCreateRequest;
import com.shield.module.announcement.entity.AnnouncementCategory;
import com.shield.module.announcement.entity.AnnouncementPriority;
import com.shield.module.announcement.entity.AnnouncementTargetAudience;
import com.shield.module.newsletter.dto.NewsletterCreateRequest;
import com.shield.module.notification.dto.NotificationBulkSendRequest;
import com.shield.module.notification.dto.NotificationSendRequest;
import com.shield.module.poll.dto.PollCreateRequest;
import com.shield.module.poll.dto.PollVoteRequest;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CommunicationModuleIT extends IntegrationTestBase {

        @Autowired
        private TenantRepository tenantRepository;
        @Autowired
        private UnitRepository unitRepository;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;

        @Test
        void testFullCommunicationFlow() {
                // 1. Create Tenant, Unit, and Users (Admin + Resident)
                TenantEntity tenant = createTenant();
                UnitEntity unit = createUnit(tenant.getId(), "101");
                UserEntity admin = createUser(tenant.getId(), unit.getId(), "Admin", "admin@comm.com", UserRole.ADMIN);
                UserEntity resident = createUser(tenant.getId(), unit.getId(), "Resident", "resident@comm.com",
                                UserRole.TENANT);

                String adminToken = login(admin.getEmail(), "password");
                String residentToken = login(resident.getEmail(), "password");

                // --- ANNOUNCEMENT ATTACHMENTS ---
                // 2. Create Announcement
                String announcementId = given()
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType("application/json")
                                .body(new AnnouncementCreateRequest(
                                                "Test Announcement",
                                                "Content",
                                                AnnouncementCategory.GENERAL,
                                                AnnouncementPriority.MEDIUM,
                                                false,
                                                null,
                                                AnnouncementTargetAudience.ALL))
                                .when().post("/announcements")
                                .then().statusCode(200)
                                .extract().path("data.id");

                // 3. Add Attachment
                String attachmentId = given()
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType("application/json")
                                .body(new AnnouncementAttachmentRequest("flyer.pdf", "http://files.com/flyer.pdf",
                                                1024L,
                                                "application/pdf"))
                                .when().post("/announcements/" + announcementId + "/attachments")
                                .then().statusCode(200)
                                .body("data.fileName", equalTo("flyer.pdf"))
                                .extract().path("data.id");

                // 4. List Attachments (Resident)
                given()
                                .header("Authorization", "Bearer " + residentToken)
                                .when().get("/announcements/" + announcementId + "/attachments")
                                .then().statusCode(200)
                                .body("data", hasSize(1));

                // 5. Delete Attachment
                given()
                                .header("Authorization", "Bearer " + adminToken)
                                .when().delete("/announcements/attachments/" + attachmentId)
                                .then().statusCode(200);

                // --- POLLS ---
                // 6. Create Poll (Admin)
                String pollId = given()
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType("application/json")
                                .body(new PollCreateRequest(
                                                "Sunday Event?",
                                                "Should we have an event?",
                                                false,
                                                Instant.now().plus(7, ChronoUnit.DAYS),
                                                List.of("Yes", "No")))
                                .when().post("/polls")
                                .then().statusCode(200)
                                .extract().path("data.id");

                // 7. Activate Poll
                given()
                                .header("Authorization", "Bearer " + adminToken)
                                .when().post("/polls/" + pollId + "/activate")
                                .then().statusCode(200)
                                .body("data.status", equalTo("ACTIVE"));

                // 8. Vote (Resident)
                String optionId = given()
                                .header("Authorization", "Bearer " + residentToken)
                                .when().get("/polls/" + pollId)
                                .then().statusCode(200)
                                .extract().path("data.options[0].id");

                given()
                                .header("Authorization", "Bearer " + residentToken)
                                .contentType("application/json")
                                .body(new PollVoteRequest(UUID.fromString(optionId)))
                                .when().post("/polls/" + pollId + "/vote")
                                .then().statusCode(200);

                // 9. Check Results
                given()
                                .header("Authorization", "Bearer " + residentToken)
                                .when().get("/polls/" + pollId + "/results")
                                .then().statusCode(200)
                                .body("data.totalVotes", equalTo(1))
                                .body("data.results[0].voteCount", equalTo(1));

                // --- NEWSLETTERS ---
                // 10. Create Newsletter
                String newsletterId = given()
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType("application/json")
                                .body(new NewsletterCreateRequest(
                                                "Feb 2026",
                                                "Updates...",
                                                "Summary",
                                                "http://files.com/news.pdf",
                                                2026,
                                                2))
                                .when().post("/newsletters")
                                .then().statusCode(200)
                                .extract().path("data.id");

                // 11. Publish Newsletter
                given()
                                .header("Authorization", "Bearer " + adminToken)
                                .when().post("/newsletters/" + newsletterId + "/publish")
                                .then().statusCode(200)
                                .body("data.status", equalTo("PUBLISHED"));

                // 12. List by Year
                given()
                                .header("Authorization", "Bearer " + residentToken)
                                .when().get("/newsletters/year/2026")
                                .then().statusCode(200)
                                .body("data.content", hasSize(1));

                // --- NOTIFICATIONS ---
                // 13. Bulk Send
                given()
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType("application/json")
                                .body(new NotificationBulkSendRequest(List.of(
                                                new NotificationSendRequest(List.of(resident.getEmail()), "Hello",
                                                                "Bulk Msg"))))
                                .when().post("/notifications/send-bulk")
                                .then().statusCode(200);

                // 14. Mark all read (Resident)
                given()
                                .header("Authorization", "Bearer " + residentToken)
                                .when().post("/notifications/mark-all-read")
                                .then().statusCode(200);

                given()
                                .header("Authorization", "Bearer " + residentToken)
                                .when().get("/notifications/unread-count")
                                .then().statusCode(200)
                                .body("data", equalTo(0));
        }

        private TenantEntity createTenant() {
                TenantEntity entity = new TenantEntity();
                entity.setName("Comm Test Info");
                entity.setAddress("Test Address");
                return tenantRepository.save(entity);
        }

        private UnitEntity createUnit(UUID tenantId, String unitNumber) {
                UnitEntity unit = new UnitEntity();
                unit.setTenantId(tenantId);
                unit.setUnitNumber(unitNumber);
                unit.setBlock("A");
                unit.setType("FLAT");
                unit.setSquareFeet(BigDecimal.valueOf(1000));
                unit.setStatus(UnitStatus.ACTIVE);
                return unitRepository.save(unit);
        }

        private UserEntity createUser(UUID tenantId, UUID unitId, String name, String email, UserRole role) {
                UserEntity user = new UserEntity();
                user.setTenantId(tenantId);
                user.setUnitId(unitId);
                user.setName(name);
                user.setEmail(email);
                user.setPhone("9999999999");
                user.setPasswordHash(passwordEncoder.encode("password"));
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
                                .statusCode(200)
                                .extract()
                                .path("data.accessToken");
        }
}
