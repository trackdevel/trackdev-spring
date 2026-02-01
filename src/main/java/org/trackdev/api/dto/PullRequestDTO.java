package org.trackdev.api.dto;

import lombok.Data;

import java.time.ZonedDateTime;

/**
 * DTO for Pull Request information shown in task views
 */
@Data
public class PullRequestDTO {
    private String id;
    private String url;
    private Integer prNumber;
    private String title;
    private String state;
    private Boolean merged;
    private String repoFullName;
    private UserSummaryDTO author;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private Integer additions;
    private Integer deletions;
    private Integer changedFiles;
    /**
     * Number of lines added by this PR that still exist unchanged in the main branch.
     * This is computed dynamically by analyzing git blame, not stored in the database.
     */
    private Integer survivingLines;
}
