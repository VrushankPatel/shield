package com.shield.module.poll.entity;

import com.shield.common.entity.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "poll_vote")
public class PollVoteEntity extends TenantAwareEntity {

    @Column(name = "poll_id", nullable = false, columnDefinition = "uuid")
    private UUID pollId;

    @Column(name = "option_id", nullable = false, columnDefinition = "uuid")
    private UUID optionId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;
}
