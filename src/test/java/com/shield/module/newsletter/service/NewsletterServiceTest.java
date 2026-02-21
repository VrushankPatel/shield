package com.shield.module.newsletter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.module.newsletter.dto.NewsletterCreateRequest;
import com.shield.module.newsletter.dto.NewsletterUpdateRequest;
import com.shield.module.newsletter.entity.NewsletterEntity;
import com.shield.module.newsletter.entity.NewsletterStatus;
import com.shield.module.newsletter.repository.NewsletterRepository;
import com.shield.security.model.ShieldPrincipal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NewsletterServiceTest {

    @Mock
    private NewsletterRepository newsletterRepository;

    @Mock
    private AuditLogService auditLogService;

    private NewsletterService newsletterService;

    @BeforeEach
    void setUp() {
        newsletterService = new NewsletterService(newsletterRepository, auditLogService);
    }

    @Test
    void createShouldPersistDraftNewsletter() {
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(newsletterRepository.save(any(NewsletterEntity.class))).thenAnswer(invocation -> {
            NewsletterEntity entity = invocation.getArgument(0);
            entity.setId(id);
            entity.setCreatedAt(Instant.now());
            return entity;
        });

        var response = newsletterService.create(
                new NewsletterCreateRequest(
                        "Monthly Update",
                        "Full content",
                        "Summary",
                        "https://files/monthly.pdf",
                        2026,
                        2),
                principal(tenantId, userId));

        assertEquals(id, response.id());
        assertEquals(NewsletterStatus.DRAFT, response.status());
        verify(auditLogService).logEvent(tenantId, userId, "NEWSLETTER_CREATED", "newsletter", id, null);
    }

    @Test
    void updateShouldRejectPublishedNewsletter() {
        UUID id = UUID.randomUUID();
        NewsletterEntity entity = new NewsletterEntity();
        entity.setId(id);
        entity.setStatus(NewsletterStatus.PUBLISHED);

        when(newsletterRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        NewsletterUpdateRequest updateRequest = new NewsletterUpdateRequest("t", "c", "s", "f");
        ShieldPrincipal principal = principal(UUID.randomUUID(), UUID.randomUUID());

        assertThrows(BadRequestException.class, () -> newsletterService.update(
                id,
                updateRequest,
                principal));
    }

    @Test
    void publishShouldSetStatusAndPublishedBy() {
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        NewsletterEntity entity = new NewsletterEntity();
        entity.setId(id);
        entity.setTenantId(tenantId);
        entity.setStatus(NewsletterStatus.DRAFT);

        when(newsletterRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(newsletterRepository.save(any(NewsletterEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = newsletterService.publish(id, principal(tenantId, userId));

        assertEquals(NewsletterStatus.PUBLISHED, response.status());
        assertEquals(userId, response.publishedBy());
        verify(auditLogService).logEvent(tenantId, userId, "NEWSLETTER_PUBLISHED", "newsletter", id, null);
    }

    @Test
    void listByYearShouldReturnPagedResult() {
        NewsletterEntity entity = new NewsletterEntity();
        entity.setId(UUID.randomUUID());
        entity.setTenantId(UUID.randomUUID());
        entity.setTitle("Yearly");
        entity.setYear(2026);
        entity.setMonth(2);
        entity.setStatus(NewsletterStatus.DRAFT);

        when(newsletterRepository.findAllByYearAndDeletedFalse(anyInt(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), Pageable.ofSize(10), 1));

        var page = newsletterService.listByYear(2026, Pageable.ofSize(10));

        assertEquals(1, page.content().size());
        assertEquals("Yearly", page.content().get(0).title());
    }

    @Test
    void deleteShouldSoftDeleteNewsletter() {
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        NewsletterEntity entity = new NewsletterEntity();
        entity.setId(id);
        entity.setTenantId(tenantId);
        entity.setStatus(NewsletterStatus.DRAFT);

        when(newsletterRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(newsletterRepository.save(any(NewsletterEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        newsletterService.delete(id, principal(tenantId, userId));

        assertTrue(entity.isDeleted());
        verify(auditLogService).logEvent(tenantId, userId, "NEWSLETTER_DELETED", "newsletter", id, null);
    }

    private ShieldPrincipal principal(UUID tenantId, UUID userId) {
        return new ShieldPrincipal(userId, tenantId, "admin@shield.dev", "ADMIN");
    }
}
