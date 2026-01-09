package org.trackdev.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for Course - Complete (with subject info)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseCompleteDTO extends CourseBasicDTO {
    private SubjectBasicDTO subject;
    private Integer projectCount;
    private Integer studentCount;
}
