package org.trackdev.api.dto;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class PointsReviewMessageDTO {
    private Long id;
    private UserSummaryDTO author;
    private String content;
    private ZonedDateTime createdAt;
    private boolean canEdit;
    private boolean canDelete;
}
