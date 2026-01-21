package org.trackdev.api.dto;

import lombok.Data;

import java.time.ZonedDateTime;

/**
 * DTO for SprintPatternItem
 */
@Data
public class SprintPatternItemDTO {
    private Long id;
    private String name;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private Integer orderIndex;
}
