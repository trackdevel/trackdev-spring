package org.trackdev.api.dto;

import lombok.Data;

import java.util.Collection;
import java.util.Date;

/**
 * DTO for Task - Basic level
 */
@Data
public class TaskBasicDTO {
    private Long id;
    private Integer taskNumber;
    private String taskKey;
    private String name;
    private String description;
    private String type;
    private Date createdAt;
    private UserSummaryDTO reporter;
    private UserSummaryDTO assignee;
    private String status;
    private String statusText;
    private Integer estimationPoints;
    private Integer rank;
    private Boolean frozen;
    private Long parentTaskId;
    private Collection<TaskBasicDTO> childTasks;
    private TaskBasicDTO parentTask;
    private Collection<SprintBasicDTO> activeSprints;
    private Collection<PullRequestDTO> pullRequests;
}
