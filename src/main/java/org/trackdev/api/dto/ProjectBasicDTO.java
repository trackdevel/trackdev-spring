package org.trackdev.api.dto;

import lombok.Data;

/**
 * DTO for Project - Basic level (embedded in user, courses, etc.)
 */
@Data
public class ProjectBasicDTO {
    private Long id;
    private String slug;
    private String name;
    private Double qualification;
    private CourseBasicDTO course;
}
