package org.trackdev.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

/**
 * DTO for Subject - Complete (with courses list)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectCompleteDTO extends SubjectBasicDTO {
    private Collection<CourseBasicDTO> courses;
}
