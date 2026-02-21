package com.shield.module.poll.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shield.audit.service.AuditLogService;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.poll.dto.PollCreateRequest;
import com.shield.module.poll.dto.PollResultsResponse;
import com.shield.module.poll.dto.PollUpdateRequest;
import com.shield.module.poll.dto.PollVoteRequest;
import com.shield.module.poll.dto.PollVoteResponse;
import com.shield.module.poll.entity.PollEntity;
import com.shield.module.poll.entity.PollOptionEntity;
import com.shield.module.poll.entity.PollStatus;
import com.shield.module.poll.entity.PollVoteEntity;
import com.shield.module.poll.repository.PollOptionRepository;
import com.shield.module.poll.repository.PollRepository;
import com.shield.module.poll.repository.PollVoteRepository;
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

@ExtendWith(MockitoExtension.class)
class PollServiceTest {

    @Mock
    private PollRepository pollRepository;

    @Mock
    private PollOptionRepository pollOptionRepository;

    @Mock
    private PollVoteRepository pollVoteRepository;

    @Mock
    private AuditLogService auditLogService;

    private PollService pollService;

    @BeforeEach
    void setUp() {
        pollService = new PollService(pollRepository, pollOptionRepository, pollVoteRepository, auditLogService);
    }

    @Test
    void createShouldPersistPollAndOptions() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID pollId = UUID.randomUUID();

        ShieldPrincipal principal = principal(tenantId, userId);

        when(pollRepository.save(any(PollEntity.class))).thenAnswer(invocation -> {
            PollEntity entity = invocation.getArgument(0);
            entity.setId(pollId);
            entity.setCreatedAt(Instant.now());
            return entity;
        });

        PollOptionEntity firstOption = new PollOptionEntity();
        firstOption.setId(UUID.randomUUID());
        firstOption.setPollId(pollId);
        firstOption.setOptionText("Yes");
        firstOption.setDisplayOrder(0);
        firstOption.setCreatedAt(Instant.now());

        PollOptionEntity secondOption = new PollOptionEntity();
        secondOption.setId(UUID.randomUUID());
        secondOption.setPollId(pollId);
        secondOption.setOptionText("No");
        secondOption.setDisplayOrder(1);
        secondOption.setCreatedAt(Instant.now());

        when(pollOptionRepository.findAllByPollIdAndDeletedFalse(pollId)).thenReturn(List.of(firstOption, secondOption));

        var response = pollService.create(new PollCreateRequest(
                "Approve budget",
                "Vote on annual budget",
                false,
                Instant.now().plusSeconds(3600),
                List.of("Yes", "No")),
                principal);

        assertEquals(pollId, response.id());
        assertEquals(2, response.options().size());
        assertEquals(PollStatus.DRAFT, response.status());
        verify(auditLogService).logEvent(tenantId, userId, "POLL_CREATED", "poll", pollId, null);
    }

    @Test
    void updateShouldFailWhenPollIsNotDraft() {
        UUID pollId = UUID.randomUUID();
        PollEntity entity = new PollEntity();
        entity.setId(pollId);
        entity.setStatus(PollStatus.ACTIVE);

        when(pollRepository.findByIdAndDeletedFalse(pollId)).thenReturn(Optional.of(entity));
        PollUpdateRequest updateRequest = new PollUpdateRequest("Title", "Desc", Instant.now().plusSeconds(100));
        ShieldPrincipal principal = principal(UUID.randomUUID(), UUID.randomUUID());

        assertThrows(BadRequestException.class, () -> pollService.update(
                pollId,
                updateRequest,
                principal));
    }

    @Test
    void activateShouldTransitionFromDraft() {
        UUID pollId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        PollEntity entity = new PollEntity();
        entity.setId(pollId);
        entity.setTenantId(tenantId);
        entity.setStatus(PollStatus.DRAFT);
        entity.setTitle("Poll");
        entity.setCreatedBy(userId);

        when(pollRepository.findByIdAndDeletedFalse(pollId)).thenReturn(Optional.of(entity));
        when(pollRepository.save(any(PollEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pollOptionRepository.findAllByPollIdAndDeletedFalse(pollId)).thenReturn(List.of());

        var response = pollService.activate(pollId, principal(tenantId, userId));

        assertEquals(PollStatus.ACTIVE, response.status());
    }

    @Test
    void voteShouldFailWhenPollExpired() {
        UUID pollId = UUID.randomUUID();

        PollEntity poll = new PollEntity();
        poll.setId(pollId);
        poll.setStatus(PollStatus.ACTIVE);
        poll.setExpiresAt(Instant.now().minusSeconds(10));

        when(pollRepository.findByIdAndDeletedFalse(pollId)).thenReturn(Optional.of(poll));
        PollVoteRequest voteRequest = new PollVoteRequest(UUID.randomUUID());
        ShieldPrincipal principal = principal(UUID.randomUUID(), UUID.randomUUID());

        assertThrows(BadRequestException.class, () -> pollService.vote(
                pollId,
                voteRequest,
                principal));
    }

    @Test
    void voteShouldFailWhenUserAlreadyVoted() {
        UUID pollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        PollEntity poll = new PollEntity();
        poll.setId(pollId);
        poll.setStatus(PollStatus.ACTIVE);
        poll.setExpiresAt(Instant.now().plusSeconds(300));

        when(pollRepository.findByIdAndDeletedFalse(pollId)).thenReturn(Optional.of(poll));
        when(pollVoteRepository.existsByPollIdAndUserIdAndDeletedFalse(pollId, userId)).thenReturn(true);
        PollVoteRequest voteRequest = new PollVoteRequest(UUID.randomUUID());
        ShieldPrincipal principal = principal(UUID.randomUUID(), userId);

        assertThrows(BadRequestException.class, () -> pollService.vote(
                pollId,
                voteRequest,
                principal));
    }

    @Test
    void voteShouldPersistWhenValid() {
        UUID pollId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID voteId = UUID.randomUUID();

        PollEntity poll = new PollEntity();
        poll.setId(pollId);
        poll.setStatus(PollStatus.ACTIVE);
        poll.setExpiresAt(Instant.now().plusSeconds(300));

        PollOptionEntity option = new PollOptionEntity();
        option.setId(optionId);
        option.setPollId(pollId);

        when(pollRepository.findByIdAndDeletedFalse(pollId)).thenReturn(Optional.of(poll));
        when(pollVoteRepository.existsByPollIdAndUserIdAndDeletedFalse(pollId, userId)).thenReturn(false);
        when(pollOptionRepository.findByIdAndDeletedFalse(optionId)).thenReturn(Optional.of(option));
        when(pollVoteRepository.save(any(PollVoteEntity.class))).thenAnswer(invocation -> {
            PollVoteEntity entity = invocation.getArgument(0);
            entity.setId(voteId);
            entity.setCreatedAt(Instant.now());
            return entity;
        });

        PollVoteResponse response = pollService.vote(
                pollId,
                new PollVoteRequest(optionId),
                principal(tenantId, userId));

        assertEquals(voteId, response.id());
        assertEquals(optionId, response.optionId());
        verify(auditLogService).logEvent(tenantId, userId, "POLL_VOTED", "poll", pollId, null);
    }

    @Test
    void getResultsShouldAggregateCountsAndPercentages() {
        UUID pollId = UUID.randomUUID();
        UUID option1 = UUID.randomUUID();
        UUID option2 = UUID.randomUUID();

        PollEntity poll = new PollEntity();
        poll.setId(pollId);
        poll.setTitle("Election");

        PollOptionEntity one = new PollOptionEntity();
        one.setId(option1);
        one.setPollId(pollId);
        one.setOptionText("A");

        PollOptionEntity two = new PollOptionEntity();
        two.setId(option2);
        two.setPollId(pollId);
        two.setOptionText("B");

        PollVoteEntity v1 = new PollVoteEntity();
        v1.setOptionId(option1);
        PollVoteEntity v2 = new PollVoteEntity();
        v2.setOptionId(option1);
        PollVoteEntity v3 = new PollVoteEntity();
        v3.setOptionId(option2);

        when(pollRepository.findByIdAndDeletedFalse(pollId)).thenReturn(Optional.of(poll));
        when(pollOptionRepository.findAllByPollIdAndDeletedFalse(pollId)).thenReturn(List.of(one, two));
        when(pollVoteRepository.findAllByPollIdAndDeletedFalse(pollId)).thenReturn(List.of(v1, v2, v3));

        PollResultsResponse response = pollService.getResults(pollId);

        assertEquals(3, response.totalVotes());
        assertEquals(2, response.results().size());
        assertEquals(66.67, response.results().get(0).percentage());
        assertEquals(33.33, response.results().get(1).percentage());
    }

    @Test
    void getMyVoteShouldFailWhenNotFound() {
        UUID pollId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(pollVoteRepository.findByPollIdAndUserIdAndDeletedFalse(pollId, userId)).thenReturn(Optional.empty());
        ShieldPrincipal principal = principal(UUID.randomUUID(), userId);

        assertThrows(ResourceNotFoundException.class, () -> pollService.getMyVote(pollId, principal));
    }

    private ShieldPrincipal principal(UUID tenantId, UUID userId) {
        return new ShieldPrincipal(userId, tenantId, "user@shield.dev", "ADMIN");
    }
}
