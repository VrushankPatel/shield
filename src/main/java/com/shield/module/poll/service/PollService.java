package com.shield.module.poll.service;

import com.shield.audit.service.AuditLogService;
import com.shield.common.dto.PagedResponse;
import com.shield.common.exception.BadRequestException;
import com.shield.common.exception.ResourceNotFoundException;
import com.shield.module.poll.dto.PollCreateRequest;
import com.shield.module.poll.dto.PollOptionResponse;
import com.shield.module.poll.dto.PollOptionResult;
import com.shield.module.poll.dto.PollResponse;
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
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PollService {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final PollVoteRepository pollVoteRepository;
    private final AuditLogService auditLogService;

    public PollService(
            PollRepository pollRepository,
            PollOptionRepository pollOptionRepository,
            PollVoteRepository pollVoteRepository,
            AuditLogService auditLogService) {
        this.pollRepository = pollRepository;
        this.pollOptionRepository = pollOptionRepository;
        this.pollVoteRepository = pollVoteRepository;
        this.auditLogService = auditLogService;
    }

    public PollResponse create(PollCreateRequest request, ShieldPrincipal principal) {
        PollEntity poll = new PollEntity();
        poll.setTenantId(principal.tenantId());
        poll.setTitle(request.title());
        poll.setDescription(request.description());
        poll.setMultipleChoice(request.multipleChoice());
        poll.setExpiresAt(request.expiresAt());
        poll.setStatus(PollStatus.DRAFT);
        poll.setCreatedBy(principal.userId());
        PollEntity saved = pollRepository.save(poll);

        for (int i = 0; i < request.options().size(); i++) {
            PollOptionEntity option = new PollOptionEntity();
            option.setTenantId(principal.tenantId());
            option.setPollId(saved.getId());
            option.setOptionText(request.options().get(i));
            option.setDisplayOrder(i);
            pollOptionRepository.save(option);
        }

        auditLogService.record(principal.tenantId(), principal.userId(), "POLL_CREATED", "poll", saved.getId(), null);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PollResponse getById(UUID id) {
        PollEntity entity = findPoll(id);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PollResponse> list(Pageable pageable) {
        return PagedResponse.from(pollRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<PollResponse> listActive(Pageable pageable) {
        return PagedResponse
                .from(pollRepository.findAllByStatusAndDeletedFalse(PollStatus.ACTIVE, pageable).map(this::toResponse));
    }

    public PollResponse update(UUID id, PollUpdateRequest request, ShieldPrincipal principal) {
        PollEntity entity = findPoll(id);
        if (entity.getStatus() != PollStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT polls can be updated");
        }
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setExpiresAt(request.expiresAt());
        PollEntity saved = pollRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "POLL_UPDATED", "poll", saved.getId(), null);
        return toResponse(saved);
    }

    public void delete(UUID id, ShieldPrincipal principal) {
        PollEntity entity = findPoll(id);
        entity.setDeleted(true);
        pollRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "POLL_DELETED", "poll", id, null);
    }

    public PollResponse activate(UUID id, ShieldPrincipal principal) {
        PollEntity entity = findPoll(id);
        if (entity.getStatus() != PollStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT polls can be activated");
        }
        entity.setStatus(PollStatus.ACTIVE);
        PollEntity saved = pollRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "POLL_ACTIVATED", "poll", saved.getId(), null);
        return toResponse(saved);
    }

    public PollResponse deactivate(UUID id, ShieldPrincipal principal) {
        PollEntity entity = findPoll(id);
        if (entity.getStatus() != PollStatus.ACTIVE) {
            throw new BadRequestException("Only ACTIVE polls can be deactivated");
        }
        entity.setStatus(PollStatus.CLOSED);
        PollEntity saved = pollRepository.save(entity);
        auditLogService.record(principal.tenantId(), principal.userId(), "POLL_DEACTIVATED", "poll", saved.getId(),
                null);
        return toResponse(saved);
    }

    public PollVoteResponse vote(UUID pollId, PollVoteRequest request, ShieldPrincipal principal) {
        PollEntity poll = findPoll(pollId);
        if (poll.getStatus() != PollStatus.ACTIVE) {
            throw new BadRequestException("Poll is not active for voting");
        }
        if (poll.getExpiresAt() != null && Instant.now().isAfter(poll.getExpiresAt())) {
            throw new BadRequestException("Poll has expired");
        }
        if (pollVoteRepository.existsByPollIdAndUserIdAndDeletedFalse(pollId, principal.userId())) {
            throw new BadRequestException("You have already voted on this poll");
        }

        pollOptionRepository.findByIdAndDeletedFalse(request.optionId())
                .filter(opt -> opt.getPollId().equals(pollId))
                .orElseThrow(() -> new BadRequestException("Invalid option for this poll"));

        PollVoteEntity vote = new PollVoteEntity();
        vote.setTenantId(principal.tenantId());
        vote.setPollId(pollId);
        vote.setOptionId(request.optionId());
        vote.setUserId(principal.userId());
        PollVoteEntity saved = pollVoteRepository.save(vote);

        auditLogService.record(principal.tenantId(), principal.userId(), "POLL_VOTED", "poll", pollId, null);
        return new PollVoteResponse(saved.getId(), saved.getPollId(), saved.getOptionId(), saved.getUserId(),
                saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public PollResultsResponse getResults(UUID pollId) {
        PollEntity poll = findPoll(pollId);
        List<PollOptionEntity> options = pollOptionRepository.findAllByPollIdAndDeletedFalse(pollId);
        List<PollVoteEntity> votes = pollVoteRepository.findAllByPollIdAndDeletedFalse(pollId);
        long totalVotes = votes.size();

        List<PollOptionResult> results = options.stream().map(option -> {
            long count = votes.stream().filter(v -> v.getOptionId().equals(option.getId())).count();
            double pct = totalVotes > 0 ? (double) count / totalVotes * 100.0 : 0.0;
            return new PollOptionResult(option.getId(), option.getOptionText(), count, Math.round(pct * 100.0) / 100.0);
        }).toList();

        return new PollResultsResponse(pollId, poll.getTitle(), totalVotes, results);
    }

    @Transactional(readOnly = true)
    public PollVoteResponse getMyVote(UUID pollId, ShieldPrincipal principal) {
        PollVoteEntity vote = pollVoteRepository.findByPollIdAndUserIdAndDeletedFalse(pollId, principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("No vote found for this poll"));
        return new PollVoteResponse(vote.getId(), vote.getPollId(), vote.getOptionId(), vote.getUserId(),
                vote.getCreatedAt());
    }

    private PollEntity findPoll(UUID id) {
        return pollRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found: " + id));
    }

    private PollResponse toResponse(PollEntity entity) {
        List<PollOptionResponse> options = pollOptionRepository.findAllByPollIdAndDeletedFalse(entity.getId())
                .stream()
                .map(opt -> new PollOptionResponse(opt.getId(), opt.getOptionText(), opt.getDisplayOrder(),
                        opt.getCreatedAt()))
                .toList();

        return new PollResponse(
                entity.getId(),
                entity.getTenantId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.isMultipleChoice(),
                entity.getExpiresAt(),
                entity.getCreatedBy(),
                options,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
