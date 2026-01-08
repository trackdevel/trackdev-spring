package org.trackdev.api.dto;

import lombok.Data;

import java.util.Date;

/**
 * DTO for Sprint - Basic level
 */
@Data
public class SprintBasicDTO {
    private Long id;
    private String name;
    private Date startDate;
    private Date endDate;
    private String status;
    private String statusText;
}
