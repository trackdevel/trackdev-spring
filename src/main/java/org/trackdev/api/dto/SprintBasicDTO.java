package org.trackdev.api.dto;

import lombok.Data;

import java.time.ZonedDateTime;

/**
 * DTO for Sprint - Basic level
 */
@Data
public class SprintBasicDTO {
    private Long id;
    private String name;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private String status;
    private String statusText;
}
