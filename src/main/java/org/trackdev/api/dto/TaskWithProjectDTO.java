package org.trackdev.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for Task - With project members (used in task detail view with project context)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskWithProjectDTO extends TaskCompleteDTO {
    private ProjectWithMembersDTO project;
}
