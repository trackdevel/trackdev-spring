package org.trackdev.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

/**
 * DTO for Sprint Board view - includes tasks organized by stories
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SprintBoardDTO extends SprintBasicDTO {
    private ProjectBasicDTO project;
    private Collection<TaskBasicDTO> tasks;
}
