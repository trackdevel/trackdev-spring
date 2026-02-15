package org.trackdev.api.dto;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Collection;

@Data
public class PointsReviewConversationDTO {
    private Long id;
    private UserSummaryDTO initiator;
    private Integer proposedPoints;
    private Collection<TaskBasicDTO> similarTasks;
    private Collection<PointsReviewMessageDTO> messages;
    private Collection<UserSummaryDTO> participants;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private boolean canEdit;
    private boolean canAddParticipant;
}
