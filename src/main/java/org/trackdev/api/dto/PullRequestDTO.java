package org.trackdev.api.dto;

import lombok.Data;

import java.util.Date;

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
    private Date createdAt;
    private Date updatedAt;
}
