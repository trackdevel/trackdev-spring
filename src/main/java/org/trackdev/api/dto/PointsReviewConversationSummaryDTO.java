package org.trackdev.api.dto;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class PointsReviewConversationSummaryDTO {
    private Long id;
    private UserSummaryDTO initiator;
    private Integer proposedPoints;
    private int messageCount;
    private ZonedDateTime createdAt;
    private ZonedDateTime lastMessageAt;
}
