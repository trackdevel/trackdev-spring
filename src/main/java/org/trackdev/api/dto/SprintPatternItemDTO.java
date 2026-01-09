package org.trackdev.api.dto;

import lombok.Data;

import java.util.Date;

/**
 * DTO for SprintPatternItem
 */
@Data
public class SprintPatternItemDTO {
    private Long id;
    private String name;
    private Date startDate;
    private Date endDate;
    private Integer orderIndex;
}
