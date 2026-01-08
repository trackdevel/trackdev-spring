package org.trackdev.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

/**
 * DTO for Sprint - Complete (with active tasks and project)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SprintCompleteDTO extends SprintBasicDTO {
    private Collection<TaskBasicDTO> activeTasks;
    private ProjectBasicDTO project;
}
