package org.trackdev.api.dto;

import lombok.Data;

/**
 * DTO for Course - Basic level (embedded in projects)
 */
@Data
public class CourseBasicDTO {
    private Long id;
    private Integer startYear;
    private String githubOrganization;
    private String language;
    private String ownerId;
    private SubjectBasicDTO subject;
}
