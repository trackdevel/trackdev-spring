package org.trackdev.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * DTO for course students list response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseStudentsResponseDTO {
    private Collection<UserSummaryDTO> students;
}
