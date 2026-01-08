package org.trackdev.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for sprints list response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SprintsResponseDTO {
    private List<SprintBasicDTO> sprints;
}
