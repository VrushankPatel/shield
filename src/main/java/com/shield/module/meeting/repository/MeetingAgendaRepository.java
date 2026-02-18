package com.shield.module.meeting.repository;

import com.shield.module.meeting.entity.MeetingAgendaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingAgendaRepository extends JpaRepository<MeetingAgendaEntity, UUID> {

    List<MeetingAgendaEntity> findAllByMeetingIdAndDeletedFalseOrderByDisplayOrderAscCreatedAtAsc(UUID meetingId);

    Optional<MeetingAgendaEntity> findByIdAndDeletedFalse(UUID id);
}
