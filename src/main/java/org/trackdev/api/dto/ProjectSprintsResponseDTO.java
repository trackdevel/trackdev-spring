package org.trackdev.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * DTO for project sprints response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSprintsResponseDTO {
    private Collection<SprintBasicDTO> sprints;
    private Long projectId;
}
