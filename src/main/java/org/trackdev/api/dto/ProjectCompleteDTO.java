package org.trackdev.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.Set;

/**
 * DTO for Project - Complete (with members, tasks, sprints)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectCompleteDTO extends ProjectBasicDTO {
    private Set<UserSummaryDTO> members;
    private Collection<TaskBasicDTO> tasks;
    private Collection<SprintBasicDTO> sprints;
}
