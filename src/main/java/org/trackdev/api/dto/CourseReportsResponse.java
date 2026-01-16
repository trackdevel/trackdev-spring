package org.trackdev.api.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for course reports list
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseReportsResponse {
    private List<ReportBasicDTO> reports;
    private Long courseId;
}
