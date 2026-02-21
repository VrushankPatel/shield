package com.shield.module.meeting.controller;

import com.shield.common.dto.ApiResponse;
import com.shield.common.dto.PagedResponse;
import com.shield.module.meeting.dto.MeetingActionItemCreateRequest;
import com.shield.module.meeting.dto.MeetingActionItemResponse;
import com.shield.module.meeting.dto.MeetingActionItemUpdateRequest;
import com.shield.module.meeting.dto.MeetingAgendaCreateRequest;
import com.shield.module.meeting.dto.MeetingAgendaOrderRequest;
import com.shield.module.meeting.dto.MeetingAgendaResponse;
import com.shield.module.meeting.dto.MeetingAgendaUpdateRequest;
import com.shield.module.meeting.dto.MeetingAttendanceMarkRequest;
import com.shield.module.meeting.dto.MeetingAttendanceReportResponse;
import com.shield.module.meeting.dto.MeetingAttendeeCreateRequest;
import com.shield.module.meeting.dto.MeetingAttendeeResponse;
import com.shield.module.meeting.dto.MeetingAttendeeRsvpRequest;
import com.shield.module.meeting.dto.MeetingCreateRequest;
import com.shield.module.meeting.dto.MeetingMinutesApproveRequest;
import com.shield.module.meeting.dto.MeetingMinutesCreateRequest;
import com.shield.module.meeting.dto.MeetingMinutesResponse;
import com.shield.module.meeting.dto.MeetingMinutesUpdateRequest;
import com.shield.module.meeting.dto.MeetingReminderResponse;
import com.shield.module.meeting.dto.MeetingResolutionCreateRequest;
import com.shield.module.meeting.dto.MeetingResolutionResponse;
import com.shield.module.meeting.dto.MeetingResolutionUpdateRequest;
import com.shield.module.meeting.dto.MeetingResponse;
import com.shield.module.meeting.dto.MeetingUpdateRequest;
import com.shield.module.meeting.dto.MeetingVoteRequest;
import com.shield.module.meeting.dto.MeetingVoteResponse;
import com.shield.module.meeting.dto.MeetingVoteResultResponse;
import com.shield.module.meeting.entity.MeetingResolutionStatus;
import com.shield.module.meeting.service.MeetingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping("/meetings")
    public ResponseEntity<ApiResponse<MeetingResponse>> create(@Valid @RequestBody MeetingCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting created", meetingService.create(request)));
    }

    @GetMapping("/meetings")
    public ResponseEntity<ApiResponse<PagedResponse<MeetingResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Meetings fetched", meetingService.list(pageable)));
    }

    @GetMapping("/meetings/{id}")
    public ResponseEntity<ApiResponse<MeetingResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting fetched", meetingService.get(id)));
    }

    @PutMapping("/meetings/{id}")
    public ResponseEntity<ApiResponse<MeetingResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting updated", meetingService.update(id, request)));
    }

    @DeleteMapping("/meetings/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        meetingService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Meeting deleted", null));
    }

    @PostMapping("/meetings/{id}/start")
    public ResponseEntity<ApiResponse<MeetingResponse>> start(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting started", meetingService.start(id)));
    }

    @PostMapping("/meetings/{id}/end")
    public ResponseEntity<ApiResponse<MeetingResponse>> end(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting ended", meetingService.end(id)));
    }

    @PostMapping("/meetings/{id}/cancel")
    public ResponseEntity<ApiResponse<MeetingResponse>> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting cancelled", meetingService.cancel(id)));
    }

    @GetMapping("/meetings/upcoming")
    public ResponseEntity<ApiResponse<PagedResponse<MeetingResponse>>> upcoming(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Upcoming meetings fetched", meetingService.listUpcoming(pageable)));
    }

    @GetMapping("/meetings/past")
    public ResponseEntity<ApiResponse<PagedResponse<MeetingResponse>>> past(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Past meetings fetched", meetingService.listPast(pageable)));
    }

    @GetMapping("/meetings/type/{type}")
    public ResponseEntity<ApiResponse<PagedResponse<MeetingResponse>>> byType(@PathVariable String type, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Meetings by type fetched", meetingService.listByType(type, pageable)));
    }

    @GetMapping("/meetings/{id}/agenda")
    public ResponseEntity<ApiResponse<List<MeetingAgendaResponse>>> listAgenda(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting agenda fetched", meetingService.listAgenda(id)));
    }

    @PostMapping("/meetings/{id}/agenda")
    public ResponseEntity<ApiResponse<MeetingAgendaResponse>> createAgenda(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingAgendaCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting agenda created", meetingService.createAgenda(id, request)));
    }

    @PutMapping("/agenda/{id}")
    public ResponseEntity<ApiResponse<MeetingAgendaResponse>> updateAgenda(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingAgendaUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting agenda updated", meetingService.updateAgenda(id, request)));
    }

    @DeleteMapping("/agenda/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAgenda(@PathVariable UUID id) {
        meetingService.deleteAgenda(id);
        return ResponseEntity.ok(ApiResponse.ok("Meeting agenda deleted", null));
    }

    @PatchMapping("/agenda/{id}/order")
    public ResponseEntity<ApiResponse<MeetingAgendaResponse>> reorderAgenda(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingAgendaOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting agenda reordered", meetingService.reorderAgenda(id, request)));
    }

    @GetMapping("/meetings/{id}/attendees")
    public ResponseEntity<ApiResponse<List<MeetingAttendeeResponse>>> listAttendees(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting attendees fetched", meetingService.listAttendees(id)));
    }

    @PostMapping("/meetings/{id}/attendees")
    public ResponseEntity<ApiResponse<MeetingAttendeeResponse>> addAttendee(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingAttendeeCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting attendee added", meetingService.addAttendee(id, request)));
    }

    @DeleteMapping("/meetings/{meetingId}/attendees/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeAttendee(@PathVariable UUID meetingId, @PathVariable UUID userId) {
        meetingService.removeAttendee(meetingId, userId);
        return ResponseEntity.ok(ApiResponse.ok("Meeting attendee removed", null));
    }

    @PostMapping("/meetings/{id}/rsvp")
    public ResponseEntity<ApiResponse<MeetingAttendeeResponse>> rsvp(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingAttendeeRsvpRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting RSVP updated", meetingService.rsvp(id, request)));
    }

    @PostMapping("/meetings/{id}/mark-attendance")
    public ResponseEntity<ApiResponse<MeetingAttendeeResponse>> markAttendance(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingAttendanceMarkRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting attendance marked", meetingService.markAttendance(id, request)));
    }

    @GetMapping("/meetings/{id}/attendance-report")
    public ResponseEntity<ApiResponse<MeetingAttendanceReportResponse>> attendanceReport(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting attendance report fetched", meetingService.attendanceReport(id)));
    }

    @GetMapping("/meetings/{id}/minutes")
    public ResponseEntity<ApiResponse<MeetingMinutesResponse>> getMinutes(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting minutes fetched", meetingService.getMinutesByMeeting(id)));
    }

    @PostMapping("/meetings/{id}/minutes")
    public ResponseEntity<ApiResponse<MeetingMinutesResponse>> createMinutes(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingMinutesCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting minutes created", meetingService.createMinutes(id, request)));
    }

    @PutMapping("/minutes/{id}")
    public ResponseEntity<ApiResponse<MeetingMinutesResponse>> updateMinutesRecord(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingMinutesCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting minutes updated", meetingService.updateMinutes(id, request)));
    }

    @PostMapping("/minutes/{id}/approve")
    public ResponseEntity<ApiResponse<MeetingMinutesResponse>> approveMinutes(
            @PathVariable UUID id,
            @RequestBody(required = false) MeetingMinutesApproveRequest request) {
        MeetingMinutesApproveRequest safeRequest = request == null ? new MeetingMinutesApproveRequest(null) : request;
        return ResponseEntity.ok(ApiResponse.ok("Meeting minutes approved", meetingService.approveMinutes(id, safeRequest)));
    }

    @GetMapping("/minutes/{id}/download")
    public ResponseEntity<ApiResponse<Map<String, String>>> downloadMinutes(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting minutes download details fetched", meetingService.downloadMinutes(id)));
    }

    @PostMapping("/minutes/{id}/generate-ai-summary")
    public ResponseEntity<ApiResponse<MeetingMinutesResponse>> generateAiSummary(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting AI summary generated", meetingService.generateAiSummary(id)));
    }

    // Legacy endpoint retained for compatibility.
    @PutMapping("/meetings/{id}/minutes")
    public ResponseEntity<ApiResponse<MeetingResponse>> updateMinutes(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingMinutesUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting minutes updated", meetingService.updateMinutes(id, request)));
    }

    @GetMapping("/meetings/{id}/resolutions")
    public ResponseEntity<ApiResponse<List<MeetingResolutionResponse>>> listResolutions(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting resolutions fetched", meetingService.listResolutions(id)));
    }

    @PostMapping("/meetings/{id}/resolutions")
    public ResponseEntity<ApiResponse<MeetingResolutionResponse>> createResolution(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingResolutionCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting resolution created", meetingService.createResolution(id, request)));
    }

    @PutMapping("/resolutions/{id}")
    public ResponseEntity<ApiResponse<MeetingResolutionResponse>> updateResolution(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingResolutionUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting resolution updated", meetingService.updateResolution(id, request)));
    }

    @DeleteMapping("/resolutions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteResolution(@PathVariable UUID id) {
        meetingService.deleteResolution(id);
        return ResponseEntity.ok(ApiResponse.ok("Meeting resolution deleted", null));
    }

    @GetMapping("/resolutions/{id}")
    public ResponseEntity<ApiResponse<MeetingResolutionResponse>> getResolution(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting resolution fetched", meetingService.getResolution(id)));
    }

    @GetMapping("/resolutions/status/{status}")
    public ResponseEntity<ApiResponse<List<MeetingResolutionResponse>>> byStatus(@PathVariable MeetingResolutionStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting resolutions by status fetched", meetingService.listResolutionsByStatus(status)));
    }

    @PostMapping("/resolutions/{id}/vote")
    public ResponseEntity<ApiResponse<MeetingVoteResponse>> vote(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingVoteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting resolution voted", meetingService.vote(id, request)));
    }

    @GetMapping("/resolutions/{id}/votes")
    public ResponseEntity<ApiResponse<List<MeetingVoteResponse>>> votes(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting votes fetched", meetingService.listVotes(id)));
    }

    @GetMapping("/resolutions/{id}/results")
    public ResponseEntity<ApiResponse<MeetingVoteResultResponse>> voteResults(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting vote result fetched", meetingService.voteResults(id)));
    }

    @GetMapping("/resolutions/{id}/my-vote")
    public ResponseEntity<ApiResponse<MeetingVoteResponse>> myVote(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("My meeting vote fetched", meetingService.myVote(id)));
    }

    @GetMapping("/meetings/{id}/action-items")
    public ResponseEntity<ApiResponse<List<MeetingActionItemResponse>>> listActionItems(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting action items fetched", meetingService.listActionItems(id)));
    }

    @PostMapping("/meetings/{id}/action-items")
    public ResponseEntity<ApiResponse<MeetingActionItemResponse>> createActionItem(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingActionItemCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting action item created", meetingService.createActionItem(id, request)));
    }

    @PutMapping("/action-items/{id}")
    public ResponseEntity<ApiResponse<MeetingActionItemResponse>> updateActionItem(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingActionItemUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting action item updated", meetingService.updateActionItem(id, request)));
    }

    @DeleteMapping("/action-items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteActionItem(@PathVariable UUID id) {
        meetingService.deleteActionItem(id);
        return ResponseEntity.ok(ApiResponse.ok("Meeting action item deleted", null));
    }

    @PostMapping("/action-items/{id}/complete")
    public ResponseEntity<ApiResponse<MeetingActionItemResponse>> completeActionItem(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting action item completed", meetingService.completeActionItem(id)));
    }

    @GetMapping("/action-items/assigned-to-me")
    public ResponseEntity<ApiResponse<PagedResponse<MeetingActionItemResponse>>> assignedToMe(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting action items assigned to me fetched", meetingService.listAssignedToMe(pageable)));
    }

    @GetMapping("/action-items/pending")
    public ResponseEntity<ApiResponse<PagedResponse<MeetingActionItemResponse>>> pendingActionItems(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Pending meeting action items fetched", meetingService.listPendingActionItems(pageable)));
    }

    @PostMapping("/meetings/{id}/send-reminders")
    public ResponseEntity<ApiResponse<MeetingReminderResponse>> sendReminders(
            @PathVariable UUID id,
            @RequestParam(required = false) String reminderType) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting reminder sent", meetingService.sendReminder(id, reminderType)));
    }

    @GetMapping("/meetings/{id}/reminders")
    public ResponseEntity<ApiResponse<List<MeetingReminderResponse>>> reminders(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Meeting reminders fetched", meetingService.listReminders(id)));
    }
}
