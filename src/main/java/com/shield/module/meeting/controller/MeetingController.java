package com.shield.module.meeting.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.module.meeting.dto.MeetingCreateRequest;
import com.shield.module.meeting.dto.MeetingMinutesUpdateRequest;
import com.shield.module.meeting.dto.MeetingResponse;
import com.shield.module.meeting.service.MeetingService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    public ResponseEntity<ApiResponse<MeetingResponse>> create(@Valid @RequestBody MeetingCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting created", meetingService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<MeetingResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Meetings fetched", meetingService.list(pageable)));
    }

    @PutMapping("/{id}/minutes")
    public ResponseEntity<ApiResponse<MeetingResponse>> updateMinutes(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingMinutesUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting minutes updated", meetingService.updateMinutes(id, request)));
    }
}
