package org.trackdev.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Response wrapper for list of sprint patterns
 */
@Data
@AllArgsConstructor
public class SprintPatternsResponseDTO {
    private List<SprintPatternDTO> sprintPatterns;
}
