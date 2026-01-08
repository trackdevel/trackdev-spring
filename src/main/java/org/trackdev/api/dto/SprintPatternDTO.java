package org.trackdev.api.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO for SprintPattern - includes all items
 */
@Data
public class SprintPatternDTO {
    private Long id;
    private String name;
    private Long courseId;
    private List<SprintPatternItemDTO> items;
}
