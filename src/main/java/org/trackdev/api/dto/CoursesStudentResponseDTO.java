package org.trackdev.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Collection;

/**
 * Response wrapper for student course list
 */
@Data
@AllArgsConstructor
public class CoursesStudentResponseDTO {
    private Collection<CourseStudentDTO> courses;
}
