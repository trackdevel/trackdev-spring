package org.trackdev.api.dto;

import lombok.Data;
import java.util.Collection;

/**
 * DTO for Course - Student view (minimal info with enrolled projects)
 */
@Data
public class CourseStudentDTO {
    private Long id;
    private Integer startYear;
    private SubjectBasicDTO subject;
    private Collection<ProjectBasicDTO> enrolledProjects;
}
