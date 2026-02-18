package com.shield.module.poll.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.common.util.SecurityUtils;
import com.shield.module.poll.dto.PollCreateRequest;
import com.shield.module.poll.dto.PollResponse;
import com.shield.module.poll.dto.PollResultsResponse;
import com.shield.module.poll.dto.PollUpdateRequest;
import com.shield.module.poll.dto.PollVoteRequest;
import com.shield.module.poll.dto.PollVoteResponse;
import com.shield.module.poll.service.PollService;
import com.shield.security.model.ShieldPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/polls")
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PollResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Polls fetched", pollService.list(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PollResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Poll fetched", pollService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PollResponse>> create(@Valid @RequestBody PollCreateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Poll created", pollService.create(request, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PollResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody PollUpdateRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Poll updated", pollService.update(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        pollService.delete(id, principal);
        return ResponseEntity.ok(ApiResponse.ok("Poll deleted", null));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PollResponse>> activate(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Poll activated", pollService.activate(id, principal)));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','COMMITTEE')")
    public ResponseEntity<ApiResponse<PollResponse>> deactivate(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Poll deactivated", pollService.deactivate(id, principal)));
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<ApiResponse<PollVoteResponse>> vote(@PathVariable UUID id,
            @Valid @RequestBody PollVoteRequest request) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Vote recorded", pollService.vote(id, request, principal)));
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<ApiResponse<PollResultsResponse>> getResults(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Poll results fetched", pollService.getResults(id)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PagedResponse<PollResponse>>> listActive(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Active polls fetched", pollService.listActive(pageable)));
    }

    @GetMapping("/{id}/my-vote")
    public ResponseEntity<ApiResponse<PollVoteResponse>> getMyVote(@PathVariable UUID id) {
        ShieldPrincipal principal = SecurityUtils.getCurrentPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Your vote fetched", pollService.getMyVote(id, principal)));
    }
}
