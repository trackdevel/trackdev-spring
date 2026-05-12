package org.trackdev.api.dto;

import lombok.Data;

import java.time.ZonedDateTime;

/**
 * List item for the cross-project "active points reviews" overview.
 * Carries enough task and project context for the dashboard to render the
 * conversation in a grouped list without extra lookups.
 */
@Data
public class PointsReviewActiveConversationDTO {
    private Long id;
    private UserSummaryDTO initiator;
    private Integer proposedPoints;
    private int messageCount;
    private ZonedDateTime createdAt;
    private ZonedDateTime lastMessageAt;

    private Long taskId;
    private String taskKey;
    private String taskName;

    private Long projectId;
    private String projectSlug;
    private String projectName;

    private Long courseId;
    private Integer courseStartYear;
    private String subjectName;
    private String subjectAcronym;
}
